package com.evbox.everon.ocpp.simulator.station.evse;

/**
 * Status of charge for EVSE.
 *
 * CHARGING corresponds to charging state
 * NOT_CHARGING corresponds to not charging currently
 * REMOTELY_STOPPED corresponds to charge stopped remotely
 */
public enum ChargingStatus {
    CHARGING,
    NOT_CHARGING,
    REMOTELY_STOPPED;

    public boolean isCharging() {
        return this == CHARGING;
    }

    public boolean isRemotelyStopped() {
        return this == REMOTELY_STOPPED;
    }
}
