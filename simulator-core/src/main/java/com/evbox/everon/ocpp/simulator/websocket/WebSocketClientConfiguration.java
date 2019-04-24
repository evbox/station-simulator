package com.evbox.everon.ocpp.simulator.websocket;

import lombok.Builder;

@Builder
public class WebSocketClientConfiguration {

    public static final WebSocketClientConfiguration DEFAULT_CONFIGURATION = WebSocketClientConfiguration.builder().build();

    private static final int MAX_SEND_ATTEMPTS_DEFAULT = 5;
    private static final long RECONNECT_INTERVAL_MS_DEFAULT = 5_000;
    private static final long SEND_RETRY_INTERVAL_MS = 1000L * 10;

    private final Integer maxSendAttempts;
    private final Long sendRetryIntervalMs;
    private final Long reconnectIntervalMs;

    public int getMaxSendAttempts() {
        return maxSendAttempts == null ? MAX_SEND_ATTEMPTS_DEFAULT : maxSendAttempts;
    }

    public long getSendRetryIntervalMs() {
        return sendRetryIntervalMs == null ? SEND_RETRY_INTERVAL_MS : sendRetryIntervalMs;
    }

    public long getReconnectIntervalMs() {
        return reconnectIntervalMs == null ? RECONNECT_INTERVAL_MS_DEFAULT : reconnectIntervalMs;
    }
}
