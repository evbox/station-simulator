package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.EvseStateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents Unplug message.
 */
@Getter
@AllArgsConstructor
public class Unplug implements UserMessage {

    private final Integer evseId;
    private final Integer connectorId;

    /**
     * Perform unplug logic.
     *
     * @param evseStateManager manges state of the evse for station
     */
    @Override
    public void perform(EvseStateManager evseStateManager) {
        evseStateManager.cableUnplugged(evseId, connectorId);
    }
}
