package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for user messages.
 */
@Slf4j
public class UserMessageHandler implements MessageHandler<UserMessage> {

    private final StateManager stateManager;

    /**
     * Create an instance.
     *
     * @param stateManager manges state of the evse for station
     */
    public UserMessageHandler(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    /**
     * Handle an incoming user message.
     *
     * @param userMessage user message
     */
    @Override
    public void handle(UserMessage userMessage) {
        userMessage.perform(stateManager);
    }

}
