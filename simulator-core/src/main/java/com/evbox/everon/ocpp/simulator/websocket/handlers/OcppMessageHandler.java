package com.evbox.everon.ocpp.simulator.websocket.handlers;

import com.evbox.everon.ocpp.simulator.station.handlers.MessageHandler;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OcppMessageHandler implements MessageHandler<AbstractWebSocketClientInboxMessage.OcppMessageAbstract> {

    private final WebSocketClient webSocketClient;

    public OcppMessageHandler(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void handle(AbstractWebSocketClientInboxMessage.OcppMessageAbstract message) {
        String ocppMessage = (String) message.getData().orElseThrow(() -> new IllegalArgumentException("OCPP message is null"));
        webSocketClient.getMessageSender().send(ocppMessage);
        log.info("SENT: {}", ocppMessage);
    }
}
