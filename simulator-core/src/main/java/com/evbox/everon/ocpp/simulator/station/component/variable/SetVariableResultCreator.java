package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;

@FunctionalInterface
public interface SetVariableResultCreator {

    /**
     * Factory method to create SetVariableResult
     *
     * @param attributePath  object which includes component, variable, attribute type
     * @param attributeValue contains value that needs to be set to attribute
     * @return object which includes component, variable, attribute type and attributeStatus which indicates success of an operation
     */
    SetVariableResult createResult(AttributePath attributePath, CiString.CiString1000 attributeValue, SetVariableResult.AttributeStatus attributeStatus);
}
