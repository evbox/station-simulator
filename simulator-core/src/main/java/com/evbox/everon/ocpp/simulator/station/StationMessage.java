package com.evbox.everon.ocpp.simulator.station;

import lombok.Value;

@Value
public class StationMessage {
    public enum Type {
        USER_ACTION, OCPP_MESSAGE
    }

    String stationId;
    Type type;
    Object data;
}