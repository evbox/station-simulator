package com.evbox.everon.ocpp.simulator.message;

import lombok.Value;

@Value
public class CallError {

    private RawCall rawCall;

    public CallError(RawCall rawCall) {
        this.rawCall = rawCall;
    }
}
