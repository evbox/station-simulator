package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.states.StationState;

import java.util.HashMap;
import java.util.Map;

public class StationStateFlowManager {

    private final Map<Integer, StationState> evsesStates = new HashMap<>();

    private StationDataHolder stationDataHolder;

    public StationStateFlowManager(StationDataHolder stationDataHolder) {
        this.stationDataHolder = stationDataHolder;
    }

    public void setStateForEvse(int evseId, StationState state) {
        evsesStates.put(evseId, state);
    }

    public StationState getStateForEvse(int eveseId) {
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
        return stationDataHolder.getStation();
    }

    public StationMessageSender getStationMessageSender() {
        return stationDataHolder.getStationMessageSender();
    }

    public StationPersistenceLayer getStationPersistenceLayer() {
        return stationDataHolder.getStationPersistenceLayer();
    }

    private StationState getState(int evseId) {
        StationState state = evsesStates.getOrDefault(evseId, new AvailableState());
        state.setStationTransactionManager(this);
        return state;
    }

}
