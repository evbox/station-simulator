package com.evbox.everon.ocpp.simulator.websocket;

import lombok.extern.slf4j.Slf4j;

/**
 * Send message to the websocket channel.
 */
@Slf4j
public class WebSocketMessageSender {

    private final OkHttpWebSocketClient webSocketClientAdapter;
    private final int maxSendAttempts;
    private final long sendRetryIntervalMs;

    public WebSocketMessageSender(OkHttpWebSocketClient webSocketClientAdapter, long sendRetryIntervalMs, int maxSendAttempts) {
        this.webSocketClientAdapter = webSocketClientAdapter;
        this.maxSendAttempts = maxSendAttempts;
        this.sendRetryIntervalMs = sendRetryIntervalMs;
    }

    /**
     * Send message to the websocket channel or retry. Give up after {@code maxSendAttempts} exceeded.
     *
     * @param message incoming message from station
     */
    public void send(String message) {

        int attempts = 0;
        for (; attempts < maxSendAttempts; attempts++) {
            boolean sentSuccessfully = webSocketClientAdapter.sendMessage(message);
            if (sentSuccessfully) {
                return;
            } else {
                try {
                    long millis = sendRetryIntervalMs * attempts;

                    log.error("Failed sending message, will retry in {} ms", millis);

                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("Unable to send message (attempts={}): {}", attempts, message);
    }
}
