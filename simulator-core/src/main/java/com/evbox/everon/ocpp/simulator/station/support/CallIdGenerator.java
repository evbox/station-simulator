package com.evbox.everon.ocpp.simulator.station.support;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton that generates call id. Used in the {@link com.evbox.everon.ocpp.simulator.station.StationMessageSender}.
 */
public final class CallIdGenerator {

    private final AtomicInteger callId = new AtomicInteger(1);

    /**
     * Return current call id value and increment.
     *
     * @return current call id value
     */
    public String get() {
        return String.valueOf(callId.getAndIncrement());
    }
}
