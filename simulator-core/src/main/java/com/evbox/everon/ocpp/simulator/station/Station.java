package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.message.*;
import com.evbox.everon.ocpp.simulator.station.exception.BadServerResponseException;
import com.evbox.everon.ocpp.simulator.user.interaction.UserAction;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.station.*;
import com.google.common.collect.ImmutableMap;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@FieldDefaults(makeFinal = true)
public class Station {
    private static final Logger LOGGER = LoggerFactory.getLogger(Station.class);

    private StationConfiguration configuration;
    private StationState state;

    private Executor executor;
    private BlockingQueue<StationInboxMessage> inbox;
    private BlockingQueue<WebSocketClientInboxMessage> webSocketInbox;
    private TransactionSequence transactionSequence = new TransactionSequence();
    private PayloadFactory payloadFactory = new PayloadFactory();

    private HeartbeatScheduler heartbeatScheduler;
    private RequestHandler requestHandler;

    private Map<Class, BiConsumer<String, Object>> requestHandlers;

    private Map<UserAction.Type, Consumer<UserAction>> userActionHandlers = ImmutableMap.<UserAction.Type, Consumer<UserAction>>builder()
            .put(UserAction.Type.AUTHORIZE, (action) -> onAuthorize((UserAction.Authorize) action))
            .put(UserAction.Type.PLUG, (action) -> onPlug((UserAction.Plug) action))
            .put(UserAction.Type.UNPLUG, (action) -> onUnplug((UserAction.Unplug) action))
            .build();

    private StationCallRegistry callRegistry;

    /**
     * Initializes station instance with inner state.
     *  @param executor single thread executor
     * @param inbox - inbox queue which controls current station
     * @param webSocketInbox - inbox queue of WebSocket client
     * @param configuration - initial configuration of the station
     */
    public Station(Executor executor, BlockingQueue<StationInboxMessage> inbox, BlockingQueue<WebSocketClientInboxMessage> webSocketInbox, StationConfiguration configuration) {
        this.executor = executor;
        this.inbox = inbox;
        this.webSocketInbox = webSocketInbox;
        this.configuration = configuration;
        this.state = new StationState(configuration);
        this.callRegistry = new StationCallRegistry(webSocketInbox);
        this.heartbeatScheduler = new HeartbeatScheduler(callRegistry, state);
        this.requestHandler = new RequestHandler(this, webSocketInbox);
        this.requestHandlers = ImmutableMap.<Class, BiConsumer<String, Object>>builder()
                .put(GetVariablesRequest.class, (callId, request) -> requestHandler.handle(callId, (GetVariablesRequest) request))
                .put(SetVariablesRequest.class, (callId, request) -> requestHandler.handle(callId, (SetVariablesRequest) request))
                .put(ResetRequest.class, (callId, request) -> requestHandler.handle(callId, (ResetRequest) request))
                .build();
    }

    public void start() {
        executor.execute(() -> {
            webSocketInbox.add(new WebSocketClientInboxMessage.Connect());
            start(BootNotificationRequest.Reason.POWER_UP);
            startConsumingMessages();
        });
    }

    private void start(BootNotificationRequest.Reason reason) {
        LOGGER.info("Starting with configuration: {}", configuration);
        sendBootNotification(reason, (request, response) -> {
            if (response.getStatus() == BootNotificationResponse.Status.ACCEPTED) {
                state.setCurrentTime(response.getCurrentTime());
                Integer heartbeatInterval = response.getInterval() == 0 ? configuration.getDefaultHeartbeatInterval() : response.getInterval();

                heartbeatScheduler.scheduleHeartbeat(heartbeatInterval);
                state.setHeartbeatInterval(heartbeatInterval);

                for (int i = 1; i <= configuration.getEvseCount(); i++) {
                    for (int j = 1; j <= configuration.getConnectorsPerEvseCount(); j++) {
                        sendStatusNotification(i, j);
                    }
                }
            }
        });
    }

    public StationState getState() {
        return StationState.copyOf(state);
    }

    public StationConfiguration getConfiguration() {
        return configuration;
    }

    public BlockingQueue<StationInboxMessage> getInbox() {
        return inbox;
    }

    public Map<String, Call> getSentCalls() {
        return ImmutableMap.copyOf(callRegistry.getSentCalls());
    }

    private void onPlug(UserAction.Plug plug) {
        Integer connectorId = plug.getConnectorId();
        if (state.getConnectorState(connectorId) != Evse.ConnectorState.AVAILABLE) {
            throw new IllegalStateException(String.format("Connector is not available: %s", connectorId));
        }

        Integer evseId = state.findEvseId(connectorId);

        if (!state.hasOngoingTransaction(evseId)) {
            Integer transactionId = transactionSequence.getNext();
            state.setTransactionId(evseId, transactionId);
        }

        state.plug(connectorId);

        sendStatusNotification(evseId, connectorId, (statusNotificationRequest, statusNotificationResponse) -> {
            if (state.hasAuthorizedToken(evseId)) {
                String tokenId = state.getToken(evseId);
                LOGGER.info("Station has authorised token {}", tokenId);

                sendTransactionEventUpdate(evseId, connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, tokenId, TransactionData.ChargingState.EV_DETECTED,
                        (transactionEventRequest, transactionEventResponse) -> {
                            state.lockConnector(evseId);
                            state.startCharging(evseId);

                            sendTransactionEventUpdate(evseId, connectorId, TransactionEventRequest.TriggerReason.CHARGING_STATE_CHANGED, tokenId, TransactionData.ChargingState.CHARGING);
                        });
            } else {
                sendTransactionEventStart(evseId, connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, null, TransactionData.ChargingState.EV_DETECTED);
            }
        });
    }

    private void onUnplug(UserAction.Unplug unplug) {
        Integer connectorId = unplug.getConnectorId();

        if (state.getConnectorState(connectorId) == Evse.ConnectorState.LOCKED) {
            throw new IllegalStateException("Unable to unplug locked connector: " + connectorId);
        }

        Integer evseId = state.findEvseId(connectorId);
        state.unplug(connectorId);

        sendStatusNotification(evseId, connectorId, (Subscriber<StatusNotificationRequest, StatusNotificationResponse>) (request, response) -> {
            sendTransactionEventEnded(evseId, connectorId, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
            state.clearToken(evseId);
            state.clearTransactionId(evseId);
        });
    }

    private void onAuthorize(UserAction.Authorize authorize) {
        String tokenId = authorize.getTokenId();
        List<Integer> evseIds = singletonList(authorize.getEvseId());
        LOGGER.info("in authorizeToken {}", tokenId);

        sendAuthorize(tokenId, evseIds, (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                List<Integer> authorizedEvseIds = response.getEvseId() == null || response.getEvseId().isEmpty() ? singletonList(state.getDefaultEvseId()) : response.getEvseId();
                authorizedEvseIds.forEach(evseId -> state.storeToken(evseId, tokenId));

                boolean haveOngoingTransaction = authorizedEvseIds.stream().allMatch(state::hasOngoingTransaction);

                if (!haveOngoingTransaction) {
                    Integer transactionId = transactionSequence.getNext();
                    authorizedEvseIds.forEach(evseId -> state.setTransactionId(evseId, transactionId));
                }

                boolean allCharging = authorizedEvseIds.stream().allMatch(state::isCharging);
                boolean allPlugged = authorizedEvseIds.stream().allMatch(state::isPlugged);

                if (allPlugged) {
                    startCharging(state, authorizedEvseIds);
                } else if (allCharging) {
                    stopCharging(state, authorizedEvseIds);
                } else {
                    if (haveOngoingTransaction) {
                        startCharging(state, authorizedEvseIds);
                    } else {
                        authorizedEvseIds.forEach(evseId -> sendTransactionEventStart(evseId, null, AUTHORIZED, tokenId, null));
                    }
                }
            }
        });
    }

    private void sendTransactionEventStart(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId, TransactionData.ChargingState chargingState) {
        TransactionEventRequest transactionEvent = payloadFactory.createTransactionEventStart(evseId, connectorId, reason, tokenId, chargingState, state.getTransactionId(evseId),
                state.getSeqNo(evseId), state.getCurrentTime());

        callRegistry.sendRequest(ActionType.TRANSACTION_EVENT, transactionEvent);
    }

    private void sendTransactionEventUpdate(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId, TransactionData.ChargingState chargingState,
            Subscriber<TransactionEventRequest, TransactionEventResponse>... subscribers) {

        TransactionEventRequest transactionEvent = payloadFactory.createTransactionEventUpdate(evseId, connectorId, reason, tokenId, chargingState, state.getTransactionId(evseId),
                state.getSeqNo(evseId), state.getCurrentTime());

        callRegistry.subscribeAndSend(ActionType.TRANSACTION_EVENT, transactionEvent, asList(subscribers));
    }

    private void sendTransactionEventEnded(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData.StoppedReason stoppedReason,
            Subscriber<TransactionEventRequest, TransactionEventResponse>... subscribers) {
        TransactionEventRequest payload = payloadFactory.createTransactionEventEnded(evseId, connectorId, reason, stoppedReason, state.getTransactionId(evseId), state.getSeqNo(evseId),
                state.getCurrentTime());
        callRegistry.subscribeAndSend(ActionType.TRANSACTION_EVENT, payload, asList(subscribers));
    }

    private void sendAuthorize(String tokenId, List<Integer> evseIds, Subscriber<AuthorizeRequest, AuthorizeResponse>... subscribers) {
        AuthorizeRequest payload = payloadFactory.createAuthorizeRequest(tokenId, evseIds);
        callRegistry.subscribeAndSend(ActionType.AUTHORIZE, payload, asList(subscribers));
    }

    private void sendBootNotification(BootNotificationRequest.Reason reason, Subscriber<BootNotificationRequest, BootNotificationResponse>... subscribers) {
        BootNotificationRequest payload = payloadFactory.createBootNotification(reason);
        callRegistry.subscribeAndSend(ActionType.BOOT_NOTIFICATION, payload, asList(subscribers));
    }

    private void sendStatusNotification(int evseId, int connectorId, Subscriber<StatusNotificationRequest, StatusNotificationResponse>... subscribers) {
        StatusNotificationRequest payload = payloadFactory.createStatusNotification(evseId, connectorId, state.getConnectorState(connectorId), state.getCurrentTime());
        callRegistry.subscribeAndSend(ActionType.STATUS_NOTIFICATION, payload, asList(subscribers));
    }

    private void onUserAction(UserAction userAction) {
        userActionHandlers.get(userAction.getType()).accept(userAction);
    }

    private void onServerMessage(String serverMessage) {
        RawCall rawCall = RawCall.fromJson(serverMessage);

        if (rawCall.getMessageType() == MessageType.CALL) {
            onRequest(Call.fromJson(serverMessage));
        } else {
            onResponse(rawCall);
        }
    }

    private void onRequest(Call call) {
        ActionType actionType = call.getActionType();

        Optional<BiConsumer<String, Object>> optionalRequestHandler = Optional.ofNullable(requestHandlers.get(actionType.getRequestType()));
        optionalRequestHandler.ifPresent(handler -> handler.accept(call.getMessageId(), call.getPayload()));
    }

    private void onResponse(RawCall rawCall) {
        String messageId = rawCall.getMessageId();

        Optional<Call> callOptional = Optional.ofNullable(callRegistry.getSentCalls().get(messageId));

        if (callOptional.isPresent()) {
            Call call = callOptional.get();

            if (rawCall.getMessageType() == MessageType.CALL_RESULT) {
                ActionType actionType = call.getActionType();
                CallResult callResult = CallResult.from(rawCall);

                callRegistry.fulfillSubscription(call.getMessageId(), callResult.getPayload(actionType.getResponseType()));
            }
        } else {
            LOGGER.error("Unexpected message {}", rawCall);
            throw new BadServerResponseException("Station '" + configuration.getStationId() + "' did not send call with messageId: " + messageId);
        }
    }

    void resetStation() {
        List<Integer> evseIds = state.getEvses();
        evseIds.forEach(evseId -> {
            if (state.hasOngoingTransaction(evseId)) {
                state.stopCharging(evseId);
                Integer connectorId = state.unlockConnector(evseId);
                sendTransactionEventEnded(evseId, connectorId, REMOTE_STOP, TransactionData.StoppedReason.IMMEDIATE_RESET, (request, response) -> {
                    reboot();
                });
            } else {
                reboot();
            }
        });
    }

    private void reboot() {
        state.clearTokens();
        state.clearTransactions();
        webSocketInbox.add(new WebSocketClientInboxMessage.Disconnect());
        webSocketInbox.add(new WebSocketClientInboxMessage.Connect());
        start(BootNotificationRequest.Reason.REMOTE_RESET);
    }

    private void startCharging(StationState state, List<Integer> authorizedEvseIds) {
        authorizedEvseIds.forEach(evseId -> {
            Integer connectorId = state.lockConnector(evseId);
            state.startCharging(evseId);
            sendTransactionEventUpdate(evseId, connectorId, AUTHORIZED, null, TransactionData.ChargingState.CHARGING);
        });
    }

    private void stopCharging(StationState state, List<Integer> authorizedEvseIds) {
        authorizedEvseIds.forEach(evseId -> {
            state.stopCharging(evseId);
            Integer connectorId = state.unlockConnector(evseId);
            sendTransactionEventUpdate(evseId, connectorId, STOP_AUTHORIZED, null, TransactionData.ChargingState.EV_DETECTED);
        });
    }

    private void startConsumingMessages() {
        for (;;) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            processMessage();
        }
    }

    void processMessage() {
        StationInboxMessage message;
        try {
            message = inbox.take();
            if (message.getType() == StationInboxMessage.Type.USER_ACTION) {
                onUserAction((UserAction) message.getData());
            } else if (message.getType() == StationInboxMessage.Type.OCPP_MESSAGE) {
                onServerMessage((String) message.getData());
            }
        } catch (InterruptedException e) {
            LOGGER.error("Exception on processing message from StationInbox", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("Exception on processing message from StationInbox", e);
        }
    }
}
