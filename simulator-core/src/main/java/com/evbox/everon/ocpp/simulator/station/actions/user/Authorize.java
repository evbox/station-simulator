package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

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
     * @param stateManager manges state of the evse for station
     */
    @Override
    public CompletableFuture<UserMessageResult> perform(StateManager stateManager) {
        return stateManager.authorized(evseId, tokenId);
    }

}

