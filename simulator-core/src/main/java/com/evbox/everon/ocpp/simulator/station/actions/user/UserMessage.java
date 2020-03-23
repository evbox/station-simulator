package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a message that comes from the user. Usually message comes from the command-line.
 */
public interface UserMessage {

    /**
     * Contains a logic related to this message.
     *
     * @param stateManager manges state of the evse for station
     */
    CompletableFuture<UserMessageResult> perform(StateManager stateManager);

}
