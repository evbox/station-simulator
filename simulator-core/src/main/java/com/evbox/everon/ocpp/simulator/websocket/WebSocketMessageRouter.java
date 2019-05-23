package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.handlers.MessageHandler;
import com.evbox.everon.ocpp.simulator.websocket.handlers.ConnectMessageHandler;
import com.evbox.everon.ocpp.simulator.websocket.handlers.DisconnectMessageHandler;
import com.evbox.everon.ocpp.simulator.websocket.handlers.OcppMessageHandler;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage.Type.*;


/**
 * Router that directs an incoming message to handler-class {@link MessageHandler}.
 *
 * Message comes from station.
 */
public class WebSocketMessageRouter {

    private final Map<WebSocketClientInboxMessage.Type, MessageHandler> messageHandlers;

    /**
     * Create an instance.
     *
     * @param webSocketClient {@link WebSocketClient}
     */
    public WebSocketMessageRouter(WebSocketClient webSocketClient) {
        this.messageHandlers = ImmutableMap.<WebSocketClientInboxMessage.Type, MessageHandler>builder()
                .put(OCPP_MESSAGE, new OcppMessageHandler(webSocketClient))
                .put(CONNECT, new ConnectMessageHandler(webSocketClient))
                .put(DISCONNECT, new DisconnectMessageHandler(webSocketClient))
                .build();
    }

    /**
     * Route an incoming message to handler-class.
     *
     * @param message message coming from station
     */
    public void route(WebSocketClientInboxMessage message) {
        messageHandlers.get(message.getType()).handle(message);
    }
}
