package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;

@FunctionalInterface
public interface VariableGetter {

    GetVariableResult get(Component component, Variable variable, GetVariableDatum.AttributeType attributeType);
}
