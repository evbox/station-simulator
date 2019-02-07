package com.evbox.everon.ocpp.simulator.station;

import lombok.Value;

@Value
public class StationConfiguration {
    String stationId;
    Integer evseCount;
    Integer connectorsPerEvseCount;
    Integer defaultHeartbeatInterval;
}
