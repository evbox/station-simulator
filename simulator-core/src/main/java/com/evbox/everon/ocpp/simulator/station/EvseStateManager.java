package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.states.EvseState;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the state for the evses of a station.
 */
public class EvseStateManager {

    private final Map<Integer, EvseState> evsesStates = new HashMap<>();

    private Station station;
    private StationStore stationStore;
    private StationMessageSender stationMessageSender;

    public EvseStateManager(Station station, StationStore stationStore, StationMessageSender stationMessageSender) {
        this.station = station;
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
    }

    public void setStateForEvse(int evseId, EvseState state) {
        evsesStates.put(evseId, state);
    }

    public EvseState getStateForEvse(int eveseId) {
        return evsesStates.getOrDefault(eveseId, new AvailableState());
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
        EvseState state = evsesStates.getOrDefault(evseId, new AvailableState());
        state.setStationTransactionManager(this);
        return state;
    }

}
