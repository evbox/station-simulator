package com.evbox.everon.ocpp.simulator.station.states;

import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;

/**
 * Interface represents the state of a specific evse/station.
 */
public interface StationState {

    String getStateName();

    void setStationTransactionManager(StationStateFlowManager stationTransactionManager);

    default void onPlug(int evseId, int connectorId) { }

    default void onAuthorize(int evseId, String tokenId) { }

    default void onUnplug(int evseId, int connectorId) { }

    default void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) { }

    default void onRemoteStop(int evseId) { }
}
