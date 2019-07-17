package com.evbox.everon.ocpp.simulator.station.support;

/**
 * Singleton that generates transaction_id. Used in the handler-classes.
 */
public final class TransactionIdGenerator {

    /**
     * Transaction id thread-local
     */
    private final ThreadLocal<Integer> transactionId = ThreadLocal.withInitial(() -> 1);

    private TransactionIdGenerator() {
    }

    /**
     * Lazily return an instance of {@link TransactionIdGenerator}.
     *
     * @return an instance
     */
    public static TransactionIdGenerator getInstance() {
        return TransactionIdGeneratorHolder.INSTANCE;
    }

    /**
     * Return current transaction id value and increment.
     *
     * @return current transaction id value
     */
    public String getAndIncrement() {
        Integer value = transactionId.get();
        transactionId.set(value + 1);
        return String.format("T_%08d", value);
    }

    private static final class TransactionIdGeneratorHolder {

        private static final TransactionIdGenerator INSTANCE = new TransactionIdGenerator();
    }
}
