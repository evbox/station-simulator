package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;

@FunctionalInterface
public interface VariableSetter {

    /**
     * Update variable with value.
     *
     * @param component contains evseId, connectorId
     * @param variable contains variable name and instance
     * @param attributeType contains type of variable attribute that has to be updated (e.g. ACTUAL, MIN, MAX, TARGET)
     * @param attributeValue new value for variable
     */
    void set(Component component, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue);
}
