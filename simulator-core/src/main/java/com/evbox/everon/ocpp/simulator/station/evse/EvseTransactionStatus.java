package com.evbox.everon.ocpp.simulator.station.evse;

/**
 * Status of the EVSE transaction
 */
public enum EvseTransactionStatus {
    /**
     * No transaction has started yet.
     */
    NONE,
    /**
     * Transaction is in progress.
     */
    IN_PROGRESS,
    /**
     * Transaction has stopped.
     */
    STOPPED
}
