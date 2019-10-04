package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.EvseStateManager;
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
     * @param evseStateManager manges state of the evse for station
     */
    @Override
    public void perform(EvseStateManager evseStateManager) {
        evseStateManager.authorized(evseId, tokenId);
    }

}

