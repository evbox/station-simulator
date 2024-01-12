package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.evse.states.AbstractEvseState;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the state for the evses of a station.
 */
public class StateManager {

    private Station station;
    private StationStore stationStore;
    private StationMessageSender stationMessageSender;

    public StateManager(Station station, StationStore stationStore, StationMessageSender stationMessageSender) {
        this.station = station;
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
    }

    public void setStateForEvse(int evseId, AbstractEvseState state) {
        stationStore.findEvse(evseId).setEvseState(state);
    }

    public AbstractEvseState getStateForEvse(int eveseId) {
        return stationStore.findEvse(eveseId).getEvseState();
    }

    public CompletableFuture<UserMessageResult> cablePlugged(int evseId, int connectorId) {
        return getState(evseId).onPlug(evseId, connectorId);
    }

    public CompletableFuture<UserMessageResult> authorized(int evseId, String tokenId) {
        return getState(evseId).onAuthorize(evseId, tokenId);
    }

    public CompletableFuture<UserMessageResult> cableUnplugged(int evseId, int connectorId) {
        return getState(evseId).onUnplug(evseId, connectorId);
    }

    public CompletableFuture<UserMessageResult> faulted(int evseId, int connectorId, String errorCode, @Nullable String errorDescription) {
        stationMessageSender.sendProblemNotifyEvent(evseId, connectorId, errorCode, errorDescription);

        return CompletableFuture.completedFuture(UserMessageResult.SUCCESSFUL);
    }

    public void remoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        getState(evseId).onRemoteStart(evseId, remoteStartId, tokenId, connector);
    }

    public void remoteStop(int evseId) {
        getState(evseId).onRemoteStop(evseId);
    }

    public Station getStation() {
        return station;
    }

    public StationMessageSender getStationMessageSender() {
        return stationMessageSender;
    }

    public StationStore getStationStore() {
        return stationStore;
    }

    private AbstractEvseState getState(int evseId) {
        AbstractEvseState state = stationStore.findEvse(evseId).getEvseState();
        state.setStationTransactionManager(this);
        return state;
    }

}
