package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;

@FunctionalInterface
public interface GetVariableHandler {

    GetVariableResult handle(GetVariableDatum getVariableDatum);

}
