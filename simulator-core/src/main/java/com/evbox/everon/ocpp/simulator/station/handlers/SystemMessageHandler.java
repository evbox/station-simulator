package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.actions.system.SystemMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for system messages
 */
@Slf4j
public class SystemMessageHandler implements MessageHandler<SystemMessage> {

    private final StationStore stationStore;
    private final StationMessageSender stationMessageSender;
    private final StateManager stateManager;

    /**
     * Create an instance.
     *
     * @param stationStore stores data of station
     * @param stationMessageSender station message sender
     * @param stateManager manage state flow for evse
     */
    public SystemMessageHandler(StationStore stationStore, StationMessageSender stationMessageSender, StateManager stateManager) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
        this.stateManager = stateManager;
    }

    /**
     * Handle an incoming system message.
     *
     * @param systemMessage system message
     */
    @Override
    public void handle(SystemMessage systemMessage) {
        systemMessage.perform(stationStore, stationMessageSender, stateManager);
    }

}