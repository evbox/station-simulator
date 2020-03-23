package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract class to represents the state of a specific evse.
 */
public abstract class AbstractEvseState {

    protected StateManager stateManager;

    public void setStationTransactionManager(StateManager stationTransactionManager) {
        this.stateManager = stationTransactionManager;
    }

    public abstract String getStateName();

    public abstract CompletableFuture<UserMessageResult> onPlug(int evseId, int connectorId);

    public abstract CompletableFuture<UserMessageResult> onAuthorize(int evseId, String tokenId);

    public abstract CompletableFuture<UserMessageResult> onUnplug(int evseId, int connectorId);

    public abstract void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector);

    public abstract void onRemoteStop(int evseId);
}
