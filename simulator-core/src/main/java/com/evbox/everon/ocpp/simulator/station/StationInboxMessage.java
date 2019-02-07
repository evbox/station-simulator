package com.evbox.everon.ocpp.simulator.station;

import lombok.Value;

@Value
public class StationInboxMessage {
    public enum Type {
        USER_ACTION, OCPP_MESSAGE
    }

    Type type;
    Object data;
}