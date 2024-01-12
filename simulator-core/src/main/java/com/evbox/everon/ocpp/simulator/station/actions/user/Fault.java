package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter
@AllArgsConstructor
public class Fault implements UserMessage {

        private final @Nullable Integer evseId;
        private final @Nullable Integer connectorId;
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
