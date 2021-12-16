package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * Represents Plug message.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class Plug implements UserMessage {

    private final Integer evseId;
    private final Integer connectorId;

    /**
     * Perform Plug-in logic.
     *
     * @param stateManager manges state of the evse for station
     */
    @Override
    public CompletableFuture<UserMessageResult> perform(StateManager stateManager) {
        System.out.println("evse" + evseId + " Connector " + connectorId + " plugged"); //NOSONAR
        return stateManager.cablePlugged(evseId, connectorId);
    }
}
