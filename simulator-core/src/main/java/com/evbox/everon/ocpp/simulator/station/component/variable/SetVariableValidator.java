package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;

@FunctionalInterface
public interface SetVariableValidator {

    /**
     *
     * Since station has to reply to SetVariablesRequest immediately, validation logic should happen before update's execution. This is why validate stands as a separate operation.
     * @param component
     * @param variable
     * @param attributeType
     * @param attributeValue
     * @return
     */
    SetVariableResult validate(Component component, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue);

}
