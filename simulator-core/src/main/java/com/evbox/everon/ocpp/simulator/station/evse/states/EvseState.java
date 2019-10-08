package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;

/**
 * Interface represents the state of a specific evse.
 */
public interface EvseState {

    String getStateName();

    void setStationTransactionManager(StateManager stationTransactionManager);

    default void onPlug(int evseId, int connectorId) { }

    default void onAuthorize(int evseId, String tokenId) { }

    default void onUnplug(int evseId, int connectorId) { }

    default void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) { }

    default void onRemoteStop(int evseId) { }
}
