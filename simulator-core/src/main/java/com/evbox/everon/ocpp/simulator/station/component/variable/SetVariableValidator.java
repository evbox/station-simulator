package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;

@FunctionalInterface
public interface SetVariableValidator {

    /**
     *
     * Since station has to reply to SetVariablesRequest immediately, validation logic should happen before update's execution. This is why validate stands as a separate operation.
     * @param attributePath
     * @param attributeValue
     * @return
     */
    SetVariableResult validate(AttributePath attributePath, CiString.CiString1000 attributeValue);

}
