package com.evbox.everon.ocpp.simulator.websocket.handlers;

import com.evbox.everon.ocpp.simulator.station.handlers.MessageHandler;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;

public class ConnectMessageHandler implements MessageHandler<WebSocketClientInboxMessage.Connect> {

    private final WebSocketClient webSocketClient;

    public ConnectMessageHandler(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void handle(WebSocketClientInboxMessage.Connect message) {
        webSocketClient.getWebSocketClientAdapter().connect(webSocketClient.getWebSocketConnectionUrl());
    }
}
