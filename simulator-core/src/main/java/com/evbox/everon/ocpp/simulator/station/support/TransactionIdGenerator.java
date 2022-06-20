package com.evbox.everon.ocpp.simulator.station.support;

import java.util.UUID;

/**
 * Singleton that generates transaction_id. Used in the handler-classes.
 */
public final class TransactionIdGenerator {

    /**
     * Transaction id thread-local
     */
    private final ThreadLocal<String> transactionId = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

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
        String value = transactionId.get();
        transactionId.set(UUID.randomUUID().toString());
        return value;
    }

    private static final class TransactionIdGeneratorHolder {

        private static final TransactionIdGenerator INSTANCE = new TransactionIdGenerator();
    }
}
