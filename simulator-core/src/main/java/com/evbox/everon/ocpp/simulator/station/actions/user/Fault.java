package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Represents Fault message.
 */
@Getter
@AllArgsConstructor
public class Fault implements UserMessage {
        private final int evseId;
        private final int connectorId;
        private final String errorCode;
        private final @Nullable String errorDescription;

        /**
        * Perform Fault logic.
        *
        * @param stateManager manages state of the evse for station
        */
        @Override
        public CompletableFuture<UserMessageResult> perform(StateManager stateManager) {
            System.out.println("evse" + evseId + " connector " + connectorId + " faulted with `" + errorCode + "` \"" + errorDescription + "\""); //NOSONAR
            return stateManager.faulted(evseId, connectorId, errorCode, errorDescription);
        }
}
