package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.EvseStateManager;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for user messages.
 */
@Slf4j
public class UserMessageHandler implements MessageHandler<UserMessage> {

    private final EvseStateManager evseStateManager;

    /**
     * Create an instance.
     *
     * @param evseStateManager manges state of the evse for station
     */
    public UserMessageHandler(EvseStateManager evseStateManager) {
        this.evseStateManager = evseStateManager;
    }

    /**
     * Handle an incoming user message.
     *
     * @param userMessage user message
     */
    @Override
    public void handle(UserMessage userMessage) {
        userMessage.perform(evseStateManager);
    }

}
