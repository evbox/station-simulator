package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v201.message.centralserver.GetVariableResult;

@FunctionalInterface
public interface VariableGetter {

    /**
     * Retrieve variable's attribute value.
     *
     * @param attributePath contains path to attribute's value in scope of the station
     * @return {@link GetVariableResult} contains status of operation and value if operation was successful
     */
    GetVariableResult get(AttributePath attributePath);
}
