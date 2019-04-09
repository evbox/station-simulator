package com.evbox.everon.ocpp.simulator.websocket.handlers;

import com.evbox.everon.ocpp.simulator.station.handlers.MessageHandler;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;

public class DisconnectMessageHandler implements MessageHandler<WebSocketClientInboxMessage.Disconnect> {

    private final WebSocketClient webSocketClient;

    public DisconnectMessageHandler(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void handle(WebSocketClientInboxMessage.Disconnect message) {
        webSocketClient.getWebSocketClientAdapter().disconnect();
        webSocketClient.setConnected(false);
    }
}
