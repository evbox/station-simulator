package com.evbox.everon.ocpp.simulator.station.evse;

/**
 * Reason why charging in EVSE stopped.
 *
 * LOCALLY_STOPPED corresponds to charge locally stopped by user/token
 * REMOTELY_STOPPED corresponds to charge stopped remotely
 */
public enum ChargingStopReason {
    LOCALLY_STOPPED,
    REMOTELY_STOPPED;

    public boolean isRemotelyStopped() {
        return this == REMOTELY_STOPPED;
    }
}
