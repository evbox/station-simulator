package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.jcip.annotations.ThreadSafe;

import java.util.Objects;

/**
 * Represents EVSE transaction. It consists of transactionId and status.
 */
@Getter
@Setter
@AllArgsConstructor
@ThreadSafe
public class EvseTransaction {

    /**
     * Transaction contains only status.
     */
    public static final EvseTransaction NONE = new EvseTransaction(EvseTransactionStatus.NONE);

    private volatile int transactionId;
    private volatile EvseTransactionStatus status;

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

    @Override
    public String toString() {
        return Integer.toString(transactionId);
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

    EvseTransactionView createView() {
        return new EvseTransactionView(this.transactionId, this.status);
    }

    @Getter
    @AllArgsConstructor
    public class EvseTransactionView {

        private final int transactionId;
        private final EvseTransactionStatus status;
    }
}
