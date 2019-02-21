package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents EVSE transaction. It consists of transactionId and status.
 */
@Getter
@Setter
@AllArgsConstructor
public class EvseTransaction {

    /**
     * Transaction contains only status.
     */
    public static final EvseTransaction NONE = new EvseTransaction(EvseTransactionStatus.NONE);

    private int transactionId;
    private EvseTransactionStatus status;

    /**
     * Create transaction with the given identity.
     * By default set the status to IN_PROGRESS.
     *
     * @param transactionId transaction identity
     */
    public EvseTransaction(int transactionId) {
        Objects.requireNonNull(transactionId);

        this.transactionId = transactionId;
        this.status = EvseTransactionStatus.IN_PROGRESS;
    }

    /**
     * Create transaction with the given status.
     *
     * @param status transaction status
     */
    private EvseTransaction(EvseTransactionStatus status) {
        Objects.requireNonNull(status);

        this.status = status;
    }
}
