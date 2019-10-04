package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.actions.system.SystemMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for system messages
 */
@Slf4j
public class SystemMessageHandler implements MessageHandler<SystemMessage> {

    private final StationPersistenceLayer stationPersistenceLayer;
    private final StationMessageSender stationMessageSender;
    private final StationStateFlowManager stationStateFlowManager;

    /**
     * Create an instance.
     *
     * @param stationPersistenceLayer stores data of station
     * @param stationMessageSender station message sender
     * @param stationStateFlowManager manage state flow for evse
     */
    public SystemMessageHandler(StationPersistenceLayer stationPersistenceLayer, StationMessageSender stationMessageSender, StationStateFlowManager stationStateFlowManager) {
        this.stationPersistenceLayer = stationPersistenceLayer;
        this.stationMessageSender = stationMessageSender;
        this.stationStateFlowManager = stationStateFlowManager;
    }

    /**
     * Handle an incoming system message.
     *
     * @param systemMessage system message
     */
    @Override
    public void handle(SystemMessage systemMessage) {
        systemMessage.perform(stationPersistenceLayer, stationMessageSender, stationStateFlowManager);
    }

}