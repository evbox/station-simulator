package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;

/**
 * Represents a message that comes from the user. Usually message comes from the command-line.
 */
public interface UserMessage {

    /**
     * Contains a logic related to this message.
     *
     * @param stationState state of the station
     * @param stationMessageSender event sender of the station
     */
    void perform(StationState stationState, StationMessageSender stationMessageSender);

}
