package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for user messages.
 */
@Slf4j
public class UserMessageHandler implements MessageHandler<UserMessage> {

    private final StationStateFlowManager stationStateFlowManager;

    /**
     * Create an instance.
     *
     * @param stationStateFlowManager manges state of the evse for station
     */
    public UserMessageHandler(StationStateFlowManager stationStateFlowManager) {
        this.stationStateFlowManager = stationStateFlowManager;
    }

    /**
     * Handle an incoming user message.
     *
     * @param userMessage user message
     */
    @Override
    public void handle(UserMessage userMessage) {
        userMessage.perform(stationStateFlowManager);
    }

}
