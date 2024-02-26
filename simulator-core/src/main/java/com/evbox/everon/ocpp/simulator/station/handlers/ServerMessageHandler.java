package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.message.*;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.exceptions.BadServerResponseException;
import com.evbox.everon.ocpp.simulator.station.exceptions.UnknownActionException;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.*;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityManager;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

/**
 * Handler for messages coming for ocpp server.*
 */
@Slf4j
public class ServerMessageHandler implements MessageHandler<String> {

    private final Map<Class, OcppRequestHandler> requestHandlers;

    private final String stationId;
    private final SubscriptionRegistry subscriptionRegistry;
    private final StationMessageSender stationMessageSender;

    /**
     * Create an instance.
     *
     * @param station reference to station which accepts the requests
     * @param stationStore store station data
     * @param stationMessageSender station message sender
     * @param stateManager states flow manager for evses
     * @param stationId station identity
     * @param subscriptionRegistry station message type registry
     */
    public ServerMessageHandler(Station station, StationStore stationStore, StationMessageSender stationMessageSender, StateManager stateManager, String stationId, SubscriptionRegistry subscriptionRegistry) {

        StationComponentsHolder stationComponentsHolder = new StationComponentsHolder(station, stationStore);
        this.stationId = stationId;
        this.subscriptionRegistry = subscriptionRegistry;
        this.stationMessageSender = stationMessageSender;

        this.requestHandlers = ImmutableMap.<Class, OcppRequestHandler>builder()
                .put(GetVariablesRequest.class, new GetVariablesRequestHandler(stationComponentsHolder, stationMessageSender))
                .put(SetVariablesRequest.class, new SetVariablesRequestHandler(stationComponentsHolder, stationMessageSender))
                .put(ResetRequest.class, new ResetRequestHandler(stationStore, stationMessageSender))
                .put(InstallCertificateRequest.class, new InstallCertificateRequestHandler(stationMessageSender))
                .put(ChangeAvailabilityRequest.class, new ChangeAvailabilityRequestHandler(new AvailabilityManager(stationStore, stationMessageSender)))
                .put(GetBaseReportRequest.class, new GetBaseReportRequestHandler(Clock.systemUTC(), stationComponentsHolder, stationMessageSender))
                .put(RequestStopTransactionRequest.class, new RequestStopTransactionRequestHandler(stationStore, stationMessageSender, stateManager))
                .put(RequestStartTransactionRequest.class, new RequestStartTransactionRequestHandler(stationStore, stationMessageSender, stateManager))
                .put(SetChargingProfileRequest.class, new SetChargingProfileRequestHandler(stationMessageSender))
                .put(UnlockConnectorRequest.class, new UnlockConnectorRequestHandler(stationStore, stationMessageSender))
                .put(CertificateSignedRequest.class, new CertificateSignedRequestHandler(stationStore, stationMessageSender))
                .put(TriggerMessageRequest.class, new TriggerMessageRequestHandler(stationStore, stationMessageSender))
                .put(SetVariableMonitoringRequest.class, new SetVariableMonitoringRequestHandler(stationComponentsHolder, stationMessageSender))
                .put(ClearVariableMonitoringRequest.class, new ClearVariableMonitoringRequestHandler(stationComponentsHolder, stationMessageSender))
                .put(GetMonitoringReportRequest.class, new GetMonitoringReportRequestHandler(stationComponentsHolder, stationMessageSender))
                .put(SendLocalListRequest.class, new SendLocalListRequestHandler(stationMessageSender))
                .put(SetNetworkProfileRequest.class, new SetNetworkProfileHandler(stationMessageSender, stationStore))
                .put(CancelReservationRequest.class, new CancelReservationRequestHandler(stationMessageSender, stationStore))
                .put(ReserveNowRequest.class, (callId, request) -> stationMessageSender.sendCallResult(callId, new ReserveNowResponse().withStatus(ReserveNowStatus.REJECTED)))
                .put(CustomerInformationRequest.class, new CustomerInformationRequestHandler(stationMessageSender))
                .put(SetDisplayMessageRequest.class, DisplayMessageHandler.createSetDisplayMessageHandler(stationMessageSender, stationStore))
                .put(ClearDisplayMessageRequest.class, DisplayMessageHandler.createClearDisplayMessage(stationMessageSender, stationStore))
                .put(GetDisplayMessagesRequest.class, DisplayMessageHandler.createGetDisplayMessage(stationMessageSender, stationStore))
                .put(UpdateFirmwareRequest.class, new UpdateFirmwareMessageHandler(stationMessageSender))
                .put(DataTransferRequest.class, new DataTransferMessageHandler(stationMessageSender))
                .build();
    }

    /**
     * Handle an incoming message from ocpp server.
     *
     * @param serverMessage message from ocpp server
     */
    @Override
    public void handle(String serverMessage) {
        RawCall rawCall = RawCall.fromJson(serverMessage);

        if (rawCall.getMessageType() == MessageType.CALL) {
            onRequest(serverMessage);
        } else {
            onResponse(rawCall);
        }
    }

    private void onRequest(String serverMessage) {
        RawCall rawCall = RawCall.fromJson(serverMessage);
        try {
            onRequest(Call.from(rawCall));
        } catch (UnknownActionException e) {
            stationMessageSender.sendCallError(rawCall.getMessageId(), CallError.Code.NOT_IMPLEMENTED, e);
            throw e;
        } catch (Exception e) {
            stationMessageSender.sendCallError(rawCall.getMessageId(), CallError.Code.INTERNAL_ERROR, e);
            throw e;
        }
    }

    private void onRequest(Call call) {
        ActionType actionType = call.getActionType();

        Optional.ofNullable(requestHandlers.get(actionType.getRequestType()))
                .ifPresent(handler -> handler.handle(call.getMessageId(), call.getPayload()));
    }

    private void onResponse(RawCall rawCall) {
        String messageId = rawCall.getMessageId();

        Optional<Call> callOptional = Optional.ofNullable(stationMessageSender.getSentCalls().get(messageId));

        if (callOptional.isPresent()) {
            Call call = callOptional.get();

            if (rawCall.getMessageType() == MessageType.CALL_RESULT) {
                ActionType actionType = call.getActionType();
                CallResult callResult = CallResult.from(rawCall);

                subscriptionRegistry.fulfillSubscription(call.getMessageId(), callResult.getPayload(actionType.getResponseType()));
            }
        } else {
            log.error("Unexpected message {}", rawCall);
            throw new BadServerResponseException("Station '" + stationId + "' did not offer call with messageId: " + messageId);
        }
    }
}
