package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;

@FunctionalInterface
public interface VariableGetter {

    /**
     * Retrieve variable's attribute value.
     *
     * @param component contains evseId, connectorId
     * @param variable contains variable name and instance
     * @param attributeType contains type of variable attribute that needs to be read (e.g. ACTUAL, MIN, MAX, TARGET)
     * @return {@link GetVariableResult} contains status of operation and value if operation was successful
     */
    GetVariableResult get(Component component, Variable variable, GetVariableDatum.AttributeType attributeType);
}
