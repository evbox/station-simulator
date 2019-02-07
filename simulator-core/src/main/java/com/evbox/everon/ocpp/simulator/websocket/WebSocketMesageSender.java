package com.evbox.everon.ocpp.simulator.websocket;

import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FieldDefaults(makeFinal = true)
public class WebSocketMesageSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);

    private WebSocketClientAdapter webSocketClientAdapter;
    private int maxSendAttempts;

    public WebSocketMesageSender(WebSocketClientAdapter webSocketClientAdapter, int maxSendAttempts) {
        this.webSocketClientAdapter = webSocketClientAdapter;
        this.maxSendAttempts = maxSendAttempts;
    }

    public void send(String message) {
        boolean retry = false;
        boolean sentSuccessfully;
        int attempts = 0;
        do {
            attempts += 1;
            sentSuccessfully = webSocketClientAdapter.sendMessage(message);

            if (!sentSuccessfully) {
                if (attempts < maxSendAttempts) {
                    retry = true;
                } else {
                    LOGGER.error("Unable to send message (attempts={}): {}", attempts, message);
                    retry = false;
                }
            }
        } while (retry);
    }
}
