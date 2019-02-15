package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.handlers.MessageHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.ServerMessageHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.UserMessageHandler;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.evbox.everon.ocpp.simulator.station.StationMessage.Type.OCPP_MESSAGE;
import static com.evbox.everon.ocpp.simulator.station.StationMessage.Type.USER_ACTION;

/**
 * Router that directs an incoming message to handler-class {@link MessageHandler}.
 *
 * It can be either a user message or message coming from the ocpp server.
 */
public class StationMessageRouter {

    private final Map<StationMessage.Type, MessageHandler> messageHandlers;

    /**
     * Create an instance.
     *
     * @param serverMessageHandler an instance for handling server messages
     * @param userMessageHandler ans instance for handling user messages
     */
    public StationMessageRouter(ServerMessageHandler serverMessageHandler, UserMessageHandler userMessageHandler) {
        this.messageHandlers = ImmutableMap.of(
                USER_ACTION, userMessageHandler,
                OCPP_MESSAGE, serverMessageHandler
        );
    }

    /**
     * Route an incoming message to handler-class.
     *
     * @param stationMessage message coming from user or ocpp server
     */
    public void route(StationMessage stationMessage) {

        messageHandlers.get(stationMessage.getType()).handle(stationMessage.getData());

    }
}
