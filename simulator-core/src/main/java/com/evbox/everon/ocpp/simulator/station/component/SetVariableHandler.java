package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;

@FunctionalInterface
public interface SetVariableHandler {

    void handle(SetVariableDatum setVariableDatum);

}
