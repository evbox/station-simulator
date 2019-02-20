package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents EVSE transaction. It consists of transactionId and state.
 */
@Getter
@Setter
@AllArgsConstructor
public class EvseTransaction {

    private int transactionId;
    private EvseTransactionState state;

    /**
     * Create transaction with the given identity.
     * By default set the state to IN_PROGRESS.
     *
     * @param transactionId transaction identity
     */
    public EvseTransaction(int transactionId) {
        Objects.requireNonNull(transactionId);

        this.transactionId = transactionId;
        this.state = EvseTransactionState.IN_PROGRESS;
    }

    /**
     * Create transaction with the given state.
     *
     * @param state transaction state
     */
    public EvseTransaction(EvseTransactionState state) {
        Objects.requireNonNull(state);

        this.state = state;
    }
}
