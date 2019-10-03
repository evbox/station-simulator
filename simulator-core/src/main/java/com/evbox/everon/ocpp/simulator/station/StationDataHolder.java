package com.evbox.everon.ocpp.simulator.station;

import lombok.Data;

@Data
public class StationDataHolder {

    private final Station station;
    private final StationPersistenceLayer stationPersistenceLayer;
    private final StationMessageSender stationMessageSender;
    private final StationStateFlowManager stationStateFlowManager;

}
