package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.centralserver.*;

@FunctionalInterface
public interface VariableGetter {

    GetVariableResult get(Component component, Evse evse, Variable variable, GetVariableDatum.AttributeType attributeType);
}
