package com.evbox.everon.ocpp.simulator.station.actions.system;

import com.evbox.everon.ocpp.simulator.station.StationDataHolder;

/**
 * Represents a message that comes from the system.
 */
public interface SystemMessage {

    /**
     * Contains a logic related to this message.
     *
     * @param stationDataHolder contains reference to station's managers
     */
    void perform(StationDataHolder stationDataHolder);
}
