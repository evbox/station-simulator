package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.states.EvseState;

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

    public void setStateForEvse(int evseId, EvseState state) {
        stationStore.findEvse(evseId).setEvseState(state);
    }

    public EvseState getStateForEvse(int eveseId) {
        return stationStore.findEvse(eveseId).getEvseState();
    }

    public void cablePlugged(int evseId, int connectorId) {
        getState(evseId).onPlug(evseId, connectorId);
    }

    public void authorized(int evseId, String tokenId) {
        getState(evseId).onAuthorize(evseId, tokenId);
    }

    public void cableUnplugged(int evseId, int connectorId) {
        getState(evseId).onUnplug(evseId, connectorId);
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

    private EvseState getState(int evseId) {
        EvseState state = stationStore.findEvse(evseId).getEvseState();
        state.setStationTransactionManager(this);
        return state;
    }

}
