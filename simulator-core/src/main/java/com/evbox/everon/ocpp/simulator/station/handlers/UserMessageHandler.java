package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for user messages.
 */
@Slf4j
public class UserMessageHandler implements MessageHandler<UserMessage> {

    private final StationState state;
    private final StationMessageSender stationMessageSender;

    /**
     * Create an instance.
     *
     * @param stationState state of the station
     * @param stationMessageSender event sender of the station
     */
    public UserMessageHandler(StationState stationState, StationMessageSender stationMessageSender) {
        this.state = stationState;
        this.stationMessageSender = stationMessageSender;
    }

    /**
     * Handle an incoming user message.
     *
     * @param userMessage user message
     */
    @Override
    public void handle(UserMessage userMessage) {
        userMessage.perform(state, stationMessageSender);
    }

}
