package com.evbox.everon.ocpp.simulator.station.support;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates call id. Used in the {@link com.evbox.everon.ocpp.simulator.station.StationMessageSender}.
 */
public final class CallIdGenerator {

    private final AtomicInteger callId = new AtomicInteger(1);

    /**
     * Generates new call id.
     *
     * @return current call id value
     */
    public String generate() {
        return String.valueOf(callId.getAndIncrement());
    }
}
