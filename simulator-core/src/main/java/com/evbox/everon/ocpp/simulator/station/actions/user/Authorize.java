package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents Authorize message.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class Authorize implements UserMessage {

    private final String tokenId;
    private final Integer evseId;

    /**
     * Perform authorisation logic.
     *
     * @param stationStateFlowManager manges state of the evse for station
     */
    @Override
    public void perform(StationStateFlowManager stationStateFlowManager) {
        stationStateFlowManager.authorized(evseId, tokenId);
    }

}

