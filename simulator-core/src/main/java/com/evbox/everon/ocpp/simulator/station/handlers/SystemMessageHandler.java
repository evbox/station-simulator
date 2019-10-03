package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.StationDataHolder;
import com.evbox.everon.ocpp.simulator.station.actions.system.SystemMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for system messages
 */
@Slf4j
public class SystemMessageHandler implements MessageHandler<SystemMessage> {

    private final StationDataHolder stationDataHolder;

    /**
     * Create an instance.
     *
     * @param stationDataHolder contains reference to station's managers
     */
    public SystemMessageHandler(StationDataHolder stationDataHolder) {
        this.stationDataHolder = stationDataHolder;
    }

    /**
     * Handle an incoming system message.
     *
     * @param systemMessage system message
     */
    @Override
    public void handle(SystemMessage systemMessage) {
        systemMessage.perform(stationDataHolder);
    }

}