package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.message.*;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.exceptions.BadServerResponseException;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.GetVariablesRequestHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.OcppRequestHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.ResetRequestHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.SetVariablesRequestHandler;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

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
     * @param stationState state of the station
     * @param stationMessageSender event sender of the station
     * @param stationId station identity
     * @param subscriptionRegistry station message type registry
     */
    public ServerMessageHandler(Station station, StationState stationState, StationMessageSender stationMessageSender, String stationId, SubscriptionRegistry subscriptionRegistry) {
        this.stationId = stationId;
        this.subscriptionRegistry = subscriptionRegistry;
        this.stationMessageSender = stationMessageSender;
        this.requestHandlers = ImmutableMap.<Class, OcppRequestHandler>builder()
                .put(GetVariablesRequest.class, new GetVariablesRequestHandler(station, stationMessageSender))
                .put(SetVariablesRequest.class, new SetVariablesRequestHandler(station, stationMessageSender))
                .put(ResetRequest.class, new ResetRequestHandler(stationState, stationMessageSender))
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
            onRequest(Call.fromJson(serverMessage));
        } else {
            onResponse(rawCall);
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
