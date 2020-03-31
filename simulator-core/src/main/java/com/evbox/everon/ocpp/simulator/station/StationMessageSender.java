package com.evbox.everon.ocpp.simulator.station;

import static  com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage.OcppMessage;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.message.CallError;
import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.simulator.station.support.CallIdGenerator;
import com.evbox.everon.ocpp.simulator.station.support.LRUCache;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.evbox.everon.ocpp.v20.message.station.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Send station messages to the OCPP server.
 * <p>
 * The API of this class might be changed in the future.
 */
@Slf4j
public class StationMessageSender {

    /**
     * Max number of entries in LRU cache.
     */
    private static final int MAX_CALLS = 1_000;

    private final StationStore stationStore;
    private final SubscriptionRegistry callRegistry;
    private final WebSocketClient webSocketClient;

    private final PayloadFactory payloadFactory = new PayloadFactory();

    private final Map<String, Call> sentCallsCache = new LRUCache<>(MAX_CALLS);

    private volatile LocalDateTime timeOfLastMessageSent;

    private final CallIdGenerator callIdGenerator = new CallIdGenerator();

    public StationMessageSender(SubscriptionRegistry subscriptionRegistry, StationStore stationStore, WebSocketClient webSocketClient) {
        this.stationStore = stationStore;
        this.callRegistry = subscriptionRegistry;
        this.webSocketClient = webSocketClient;
        this.timeOfLastMessageSent = LocalDateTime.MIN;
    }

    /**
     * Send NotifyReportRequest event.
     *
     * @param requestId         request id
     * @param monitoringResult  monitors to be reported
     */
    public void sendNotifyMonitoringReport(Integer requestId, Map<ComponentVariable, List<SetMonitoringDatum>> monitoringResult) {
        List<Monitor> monitors = monitoringResult.entrySet().stream()
                                                            .map(StationMessageSender::toMonitor)
                                                            .collect(Collectors.toList());

        NotifyMonitoringReportRequest payload = new NotifyMonitoringReportRequest()
                .withRequestId(requestId)
                .withTbc(false)
                .withSeqNo(0)
                .withMonitor(monitors)
                .withGeneratedAt(ZonedDateTime.now(ZoneOffset.UTC));

        sendPayloadOfType(ActionType.NOTIFY_MONITORING_REPORT, payload);
    }

    /**
     * Send NotifyEventRequest event.
     *
     * @param eventData list of event data
     */
    public void sendNotifyEvent(List<EventDatum> eventData) {
        NotifyEventRequest payload = new NotifyEventRequest()
                .withGeneratedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .withTbc(false)
                .withSeqNo(0)
                .withEventData(eventData);

        sendPayloadOfType(ActionType.NOTIFY_EVENT, payload);
    }

    /**
     * Send TransactionEventStart event.
     *
     * @param evseId  evse identity
     * @param reason  reason why it was triggered
     * @param tokenId token identity
     */
    public void sendTransactionEventStart(Integer evseId, TransactionEventRequest.TriggerReason reason, String tokenId) {
        sendTransactionEventStart(evseId, null, null, reason, tokenId, null);
    }

    /**
     * Send TransactionEventStart event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param remoteStartId connector identity
     * @param reason        reason why it was triggered
     */
    public void sendTransactionEventStart(Integer evseId, Integer connectorId, Integer remoteStartId, TransactionEventRequest.TriggerReason reason) {
        sendTransactionEventStart(evseId, connectorId, remoteStartId, reason, null, null);
    }

    /**
     * Send TransactionEventStart event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param reason        reason why it was triggered
     * @param chargingState charging state of the station
     */
    public void sendTransactionEventStart(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData.ChargingState chargingState) {
        sendTransactionEventStart(evseId, connectorId, null, reason, null, chargingState);
    }

    /**
     * Send TransactionEventUpdate event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param reason        reason why it was triggered
     * @param chargingState charging state of the station
     */
    public void sendTransactionEventUpdate(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData.ChargingState chargingState) {
        sendTransactionEventUpdate(evseId, connectorId, reason, chargingState, null);
    }

    /**
     * Send TransactionEventUpdate event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param reason        reason why it was triggered
     * @param chargingState charging state of the station
     * @param powerConsumed power consumed by the evse
     */
    public void sendTransactionEventUpdate(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData.ChargingState chargingState, Long powerConsumed) {
        sendTransactionEventUpdate(evseId, connectorId, reason, null, chargingState, powerConsumed);
    }

    /**
     * Send TransactionEventUpdate event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param reason        reason why it was triggered
     * @param tokenId       token identity
     * @param chargingState charging state of the station
     */
    public void sendTransactionEventUpdate(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId, TransactionData.ChargingState chargingState) {
        sendTransactionEventUpdate(evseId, connectorId, reason, tokenId, chargingState, null);
    }

    /**
     * Send TransactionEventUpdate event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param reason        reason why it was triggered
     * @param tokenId       token identity
     * @param chargingState charging state of the station
     * @param powerConsumed power consumed by the evse
     */
    public void sendTransactionEventUpdate(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId, TransactionData.ChargingState chargingState, Long powerConsumed) {
        TransactionEventRequest payload = payloadFactory.createTransactionEventUpdate(stationStore.findEvse(evseId),
                connectorId, reason, tokenId, chargingState, stationStore.getCurrentTime(), Optional.ofNullable(powerConsumed).orElse(0L));

        sendPayloadOfType(ActionType.TRANSACTION_EVENT, payload);
    }

    /**
     * Send TransactionEventEnded event and subscribe on response.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param reason        reason why it was triggered
     * @param stoppedReason reason why transaction was stopped
     * @param subscriber    callback that will be executed after receiving a response from OCPP server
     */
    public void sendTransactionEventEndedAndSubscribe(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData.StoppedReason stoppedReason,
                                                      Subscriber<TransactionEventRequest, TransactionEventResponse> subscriber) {
        TransactionEventRequest payload = payloadFactory.createTransactionEventEnded(stationStore.findEvse(evseId),
                connectorId, reason, stoppedReason, stationStore.getCurrentTime());

        Call call = createAndRegisterCall(ActionType.TRANSACTION_EVENT, payload);
        callRegistry.addSubscription(call.getMessageId(), payload, subscriber);

        sendMessage(OcppMessage.builder().data(call.toJson()).build());
    }

    /**
     * Send TransactionEventEnded event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param triggerReason reason why it was triggered
     * @param stoppedReason reason why transaction was stopped
     */
    public void sendTransactionEventEnded(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason triggerReason, TransactionData.StoppedReason stoppedReason) {
        TransactionEventRequest payload = payloadFactory.createTransactionEventEnded(stationStore.findEvse(evseId),
                connectorId, triggerReason, stoppedReason, stationStore.getCurrentTime());

        sendPayloadOfType(ActionType.TRANSACTION_EVENT, payload);
    }

    /**
     * Send TransactionEventEnded event.
     *
     * @param evseId        evse identity
     * @param connectorId   connector identity
     * @param triggerReason reason why it was triggered
     * @param stoppedReason reason why transaction was stopped
     * @param powerConsumed kwh consumed during session
     */
    public void sendTransactionEventEnded(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason triggerReason, TransactionData.StoppedReason stoppedReason, long powerConsumed) {
        TransactionEventRequest payload = payloadFactory.createTransactionEventEnded(stationStore.findEvse(evseId),
                connectorId, triggerReason, stoppedReason, stationStore.getCurrentTime(), powerConsumed);

        sendPayloadOfType(ActionType.TRANSACTION_EVENT, payload);
    }

    /**
     * Send Authorize event and subscribe on response.
     *
     * @param tokenId    token identity
     * @param evseIds    evse identity
     * @param subscriber callback that will be executed after receiving a response from OCPP server
     */
    public void sendAuthorizeAndSubscribe(String tokenId, List<Integer> evseIds, Subscriber<AuthorizeRequest, AuthorizeResponse> subscriber) {
        AuthorizeRequest payload = payloadFactory.createAuthorizeRequest(tokenId, evseIds);

        Call call = createAndRegisterCall(ActionType.AUTHORIZE, payload);
        callRegistry.addSubscription(call.getMessageId(), payload, subscriber);

        sendMessage(OcppMessage.builder().data(call.toJson()).build());
    }

    /**
     * Send BootNotification event and subscribe on response.
     *
     * @param reason     reason why it was triggered
     * @param subscriber callback that will be executed after receiving a response from OCPP server
     */
    public void sendBootNotificationAndSubscribe(BootNotificationRequest.Reason reason, Subscriber<BootNotificationRequest, BootNotificationResponse> subscriber) {
        BootNotificationRequest payload = payloadFactory.createBootNotification(reason);

        Call call = createAndRegisterCall(ActionType.BOOT_NOTIFICATION, payload);
        callRegistry.addSubscription(call.getMessageId(), payload, subscriber);

        sendMessage(OcppMessage.builder().data(call.toJson()).build());
    }

    /**
     * Send BootNotification event.
     *
     * @param reason reason why it was triggered
     */
    public void sendBootNotification(BootNotificationRequest.Reason reason) {
        BootNotificationRequest payload = payloadFactory.createBootNotification(reason);

        sendPayloadOfType(ActionType.BOOT_NOTIFICATION, payload);
    }

    /**
     * Send StatusNotification event and subscribe on response.
     *
     * @param evse       {@link Evse}
     * @param connector  {@link Connector}
     * @param subscriber callback that will be executed after receiving a response from OCPP server
     */
    public void sendStatusNotificationAndSubscribe(Evse evse, Connector connector, Subscriber<StatusNotificationRequest, StatusNotificationResponse> subscriber) {
        StatusNotificationRequest payload = payloadFactory.createStatusNotification(evse, connector, stationStore.getCurrentTime());

        Call call = createAndRegisterCall(ActionType.STATUS_NOTIFICATION, payload);
        callRegistry.addSubscription(call.getMessageId(), payload, subscriber);

        sendMessage(OcppMessage.builder().data(call.toJson()).build());

    }

    /**
     * Send StatusNotification event.
     *
     * @param evseId      evse identity
     * @param connectorId connector identity
     */
    public void sendStatusNotification(int evseId, int connectorId) {
        StatusNotificationRequest payload = payloadFactory.createStatusNotification(evseId, connectorId,
                stationStore.findEvse(evseId).findConnector(connectorId).getCableStatus(), stationStore.getCurrentTime());

        sendPayloadOfType(ActionType.STATUS_NOTIFICATION, payload);
    }

    /**
     * Send StatusNotification event.
     *
     * @param evseId            evse identity
     * @param connectorId       connector identity
     * @param connectorStatus   status of the connector
     */
    public void sendStatusNotification(int evseId, int connectorId, StatusNotificationRequest.ConnectorStatus connectorStatus) {
        StatusNotificationRequest payload = payloadFactory.createStatusNotification(evseId, connectorId, connectorStatus, stationStore.getCurrentTime());

        sendPayloadOfType(ActionType.STATUS_NOTIFICATION, payload);
    }

    /**
     * Send StatusNotification event.
     *
     * @param evse      {@link Evse}
     * @param connector {@link Connector}
     */
    public void sendStatusNotification(Evse evse, Connector connector) {
        StatusNotificationRequest payload = payloadFactory.createStatusNotification(evse, connector, stationStore.getCurrentTime());

        sendPayloadOfType(ActionType.STATUS_NOTIFICATION, payload);
    }

    /**
     * Send HeartBeat event and subscribe on response.
     *
     * @param heartbeatRequest heart-beat request
     * @param subscriber       callback that will be executed after receiving a response from OCPP server
     */
    public void sendHeartBeatAndSubscribe(HeartbeatRequest heartbeatRequest, Subscriber<HeartbeatRequest, HeartbeatResponse> subscriber) {
        Call call = createAndRegisterCall(ActionType.HEARTBEAT, heartbeatRequest);
        callRegistry.addSubscription(call.getMessageId(), heartbeatRequest, subscriber);

        sendMessage(OcppMessage.builder().data(call.toJson()).build());
    }

    /**
     * Sends NotifyReport event
     *
     * @param requestId requestId from GetBaseReport
     * @param tbc to be continued, signifies if this is the last report
     * @param seqNo sequence number of this message
     * @param reportData report data containing information about variables
     */
    public void sendNotifyReport(@Nullable Integer requestId, boolean tbc, int seqNo, ZonedDateTime generatedAt, List<ReportDatum> reportData) {
        NotifyReportRequest payload =
                payloadFactory.createNotifyReportRequest(requestId, tbc, seqNo, generatedAt, reportData);

        sendPayloadOfType(ActionType.NOTIFY_REPORT, payload);
    }

    /**
     * Sends SignCertificateRequest message
     *
     * @param csr Certificate signing request
     */
    public void sendSignCertificateRequest(String csr) {
        SignCertificateRequest payload = payloadFactory.createSignCertificateRequest(csr);

        sendPayloadOfType(ActionType.SIGN_CERTIFICATE, payload);
    }

    /**
     * Sends NotifyCustomerInformationRequest containing customer data
     *
     * @param request - notify customer information request
     */
    public void sendNotifyCustomerInformationRequest(final NotifyCustomerInformationRequest request) {
        sendPayloadOfType(ActionType.NOTIFY_CUSTOMER_INFORMATION, request);
    }

    /**
     * Send an incoming message {@link AbstractWebSocketClientInboxMessage} to ocpp server.
     *
     * @param message {@link AbstractWebSocketClientInboxMessage}
     */
    public void sendMessage(AbstractWebSocketClientInboxMessage message) {
        webSocketClient.getInbox().offer(message);
        timeOfLastMessageSent = LocalDateTime.now();
    }

    /**
     * Send {@link CallResult} to ocpp server.
     *
     * @param callId  identity of the message
     * @param payload body of the message
     */
    public void sendCallResult(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        sendMessage(OcppMessage.builder().data(callStr).build());
    }

    /**
     * Send {@link CallError} to ocpp server.
     *
     * @param callId  identity of the message
     * @param errorCode the error code
     * @param payload body of the message
     */
    public void sendCallError(String callId, CallError.Code errorCode, Object payload) {
        CallError callError = new CallError(callId, errorCode, payload);
        String callStr = callError.toJson();
        sendMessage(OcppMessage.builder().data(callStr).build());
    }

    /**
     * Return unmodifiable map of registered {@link Call} calls.
     *
     * @return unmodifiable map of [callId, {@link Call}]
     */
    public Map<String, Call> getSentCalls() {
        return Collections.unmodifiableMap(sentCallsCache);
    }

    /**
     * Return the timestamp in milliseconds of the last message sent to the server.
     *
     * @return timestamp in milliseconds
     */
    public LocalDateTime getTimeOfLastMessageSent() { return timeOfLastMessageSent; }

    private static Monitor toMonitor(Map.Entry<ComponentVariable, List<SetMonitoringDatum>> entry) {
        return new Monitor()
                .withComponent(entry.getKey().getComponent())
                .withVariable(entry.getKey().getVariable())
                .withVariableMonitoring(
                        entry.getValue().stream()
                                .map(d -> new VariableMonitoring()
                                        .withId(d.getId())
                                        .withSeverity(d.getSeverity())
                                        .withTransaction(d.getTransaction())
                                        .withType(VariableMonitoring.Type.fromValue(d.getType().value()))
                                        .withValue(d.getValue()))
                                .collect(Collectors.toList()));
    }

    private void sendTransactionEventStart(Integer evseId, Integer connectorId, Integer remoteStartId, TransactionEventRequest.TriggerReason reason, String tokenId, TransactionData.ChargingState chargingState) {
        TransactionEventRequest transactionEvent = payloadFactory.createTransactionEventStart(stationStore.findEvse(evseId),
                connectorId, reason, tokenId, chargingState, remoteStartId, stationStore.getCurrentTime());

        sendPayloadOfType(ActionType.TRANSACTION_EVENT, transactionEvent);
    }

    private <T> void sendPayloadOfType(ActionType type, T payload) {
        Call call = createAndRegisterCall(type, payload);
        sendMessage(OcppMessage.builder().data(call.toJson()).build());
    }

    private <T> Call createAndRegisterCall(ActionType actionType, T payload) {

        String callId = callIdGenerator.generate();

        Call call = new Call(callId, actionType, payload);

        sentCallsCache.put(callId, call);
        return call;
    }

}
