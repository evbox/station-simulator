package com.evbox.everon.ocpp.simulator.station.evse;

/**
 * State of the EVSE transaction
 */
public enum EvseTransactionState {
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
