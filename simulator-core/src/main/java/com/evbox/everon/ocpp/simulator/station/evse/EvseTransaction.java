package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.v201.message.station.ChargingState;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents EVSE transaction. It consists of transactionId and status.
 */
@Getter
@Setter
public class EvseTransaction {

    /**
     * Transaction contains only status.
     */
    public static final EvseTransaction NONE = new EvseTransaction(EvseTransactionStatus.NONE);

    private String transactionId;
    private EvseTransactionStatus status;
    private ChargingState chargingState;

    /**
     * Create transaction with the given identity.
     * By default set the status to IN_PROGRESS.
     *
     * @param transactionId transaction identity
     */
    public EvseTransaction(String transactionId) {
        Objects.requireNonNull(transactionId);

        this.transactionId = transactionId;
        this.status = EvseTransactionStatus.IN_PROGRESS;
    }

    /**
     * Create transaction with the given status.
     *
     * @param status transaction status
     */
    public EvseTransaction(EvseTransactionStatus status) {
        Objects.requireNonNull(status);

        this.status = status;
    }

    public EvseTransaction(String transactionId, EvseTransactionStatus status) {
        this.transactionId = transactionId;
        this.status = status;
    }

    @Override
    public String toString() {
        return transactionId;
    }

    EvseTransactionView createView() {
        return new EvseTransactionView(this.transactionId, this.status);
    }

    public ChargingState updateChargingStateIfChanged(ChargingState newState) {
        Preconditions.checkNotNull(newState);

        boolean stateChanged = chargingState == null || !chargingState.equals(newState);
        if (stateChanged) {
            chargingState = newState;
            return newState;
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public class EvseTransactionView {

        private final String transactionId;
        private final EvseTransactionStatus status;
    }
}
