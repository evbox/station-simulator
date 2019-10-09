package com.evbox.everon.ocpp.simulator.station.actions.system;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;

/**
 * Represents a message that comes from the system.
 */
public interface SystemMessage {

    /**
     * Contains a logic related to this message.
     *
     * @param stationStore stores data of station
     * @param stationMessageSender station message sender
     * @param stateManager manage state flow for evse
     */
    void perform(StationStore stationStore, StationMessageSender stationMessageSender, StateManager stateManager);
}
