package com.evbox.everon.ocpp.simulator.station.support;

/**
 * Singleton that generates call id. Used in the {@link com.evbox.everon.ocpp.simulator.station.StationMessageSender}.
 */
public final class CallIdGenerator {

    /**
     * Call id thread-local
     */
    private final ThreadLocal<Integer> callId = ThreadLocal.withInitial(() -> 1);

    private CallIdGenerator() {
    }

    /**
     * Lazily return an instance of {@link CallIdGenerator}.
     *
     * @return an instance
     */
    public static CallIdGenerator getInstance() {
        return CallIdGeneratorHolder.INSTANCE;
    }

    /**
     * Return current call id value and increment.
     *
     * @return current call id value
     */
    public Integer getAndIncrement() {
        Integer value = callId.get();
        callId.set(value + 1);
        return value;
    }

    private static final class CallIdGeneratorHolder {

        private static final CallIdGenerator INSTANCE = new CallIdGenerator();
    }
}
