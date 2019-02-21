package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;

@FunctionalInterface
public interface SetVariableValidator {

    SetVariableResult validate(Component component, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue);

}
