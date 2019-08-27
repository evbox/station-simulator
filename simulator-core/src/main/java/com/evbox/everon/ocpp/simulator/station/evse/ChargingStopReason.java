package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.v20.message.station.TransactionData;

/**
 * Reason why charging in EVSE stopped.
 *
 * LOCALLY_STOPPED corresponds to charge locally stopped by user/token
 * REMOTELY_STOPPED corresponds to charge stopped remotely
 */
public enum ChargingStopReason {
    NONE(null),
    LOCALLY_STOPPED(TransactionData.StoppedReason.EV_DISCONNECTED),
    REMOTELY_STOPPED(TransactionData.StoppedReason.REMOTE);

    TransactionData.StoppedReason stoppedReason;

    ChargingStopReason(TransactionData.StoppedReason stoppedReason) {
        this.stoppedReason = stoppedReason;
    }

    public TransactionData.StoppedReason getStoppedReason() {
        return stoppedReason;
    }
}
