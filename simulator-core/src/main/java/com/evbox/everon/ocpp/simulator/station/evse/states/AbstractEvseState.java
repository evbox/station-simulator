package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;

/**
 * Abstract class to represents the state of a specific evse.
 */
public abstract class AbstractEvseState {

    protected StateManager stateManager;

    public void setStationTransactionManager(StateManager stationTransactionManager) {
        this.stateManager = stationTransactionManager;
    }

    public abstract String getStateName();

    public abstract void onPlug(int evseId, int connectorId);

    public abstract void onAuthorize(int evseId, String tokenId);

    public abstract void onUnplug(int evseId, int connectorId);

    public abstract void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector);

    public abstract void onRemoteStop(int evseId);
}
