package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.v201.message.station.Reason;

/**
 * Reason why charging in EVSE stopped.
 *
 * LOCALLY_STOPPED corresponds to charge locally stopped by user/token
 * REMOTELY_STOPPED corresponds to charge stopped remotely
 */
public enum ChargingStopReason {
    NONE(null),
    LOCALLY_STOPPED(Reason.EV_DISCONNECTED),
    REMOTELY_STOPPED(Reason.REMOTE);

    Reason stoppedReason;

    ChargingStopReason(Reason stoppedReason) {
        this.stoppedReason = stoppedReason;
    }

    public Reason getStoppedReason() {
        return stoppedReason;
    }
}
