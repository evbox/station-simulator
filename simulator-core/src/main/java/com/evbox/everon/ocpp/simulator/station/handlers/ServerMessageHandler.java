package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.message.*;
import com.evbox.everon.ocpp.simulator.station.*;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.exceptions.BadServerResponseException;
import com.evbox.everon.ocpp.simulator.station.exceptions.UnknownActionException;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.*;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityManager;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.RequestStartTransactionRequest;
import com.evbox.everon.ocpp.v20.message.station.RequestStopTransactionRequest;
import com.evbox.everon.ocpp.v20.message.station.SetChargingProfileRequest;
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
     * @param stationStateFlowManager states flow manager for evses
     * @param stationId station identity
     * @param subscriptionRegistry station message type registry
     */
    public ServerMessageHandler(Station station, StationStore stationStore, StationMessageSender stationMessageSender, StationStateFlowManager stationStateFlowManager, String stationId, SubscriptionRegistry subscriptionRegistry) {

        StationComponentsHolder stationComponentsHolder = new StationComponentsHolder(station, stationStore);
        this.stationId = stationId;
        this.subscriptionRegistry = subscriptionRegistry;
        this.stationMessageSender = stationMessageSender;

        this.requestHandlers = ImmutableMap.<Class, OcppRequestHandler>builder()
                .put(GetVariablesRequest.class, new GetVariablesRequestHandler(stationComponentsHolder, stationMessageSender))
                .put(SetVariablesRequest.class, new SetVariablesRequestHandler(stationComponentsHolder, stationMessageSender))
                .put(ResetRequest.class, new ResetRequestHandler(stationStore, stationMessageSender))
                .put(ChangeAvailabilityRequest.class, new ChangeAvailabilityRequestHandler(new AvailabilityManager(stationStore, stationMessageSender)))
                .put(GetBaseReportRequest.class, new GetBaseReportRequestHandler(Clock.systemUTC(), stationComponentsHolder, stationMessageSender))
                .put(RequestStopTransactionRequest.class, new RequestStopTransactionRequestHandler(stationStore, stationMessageSender, stationStateFlowManager))
                .put(RequestStartTransactionRequest.class, new RequestStartTransactionRequestHandler(stationStore, stationMessageSender, stationStateFlowManager))
                .put(SetChargingProfileRequest.class, new SetChargingProfileRequestHandler(stationMessageSender))
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
