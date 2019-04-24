package com.evbox.everon.ocpp.simulator.websocket.handlers;

import com.evbox.everon.ocpp.simulator.station.handlers.MessageHandler;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OcppMessageHandler implements MessageHandler<WebSocketClientInboxMessage.OcppMessage> {

    private final WebSocketClient webSocketClient;

    public OcppMessageHandler(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void handle(WebSocketClientInboxMessage.OcppMessage message) {
        String ocppMessage = (String) message.getData().orElseThrow(() -> new IllegalArgumentException("OCPP message is null"));
        webSocketClient.getMessageSender().send(ocppMessage);
        log.info("SENT: {}", ocppMessage);
    }
}
