package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;

@FunctionalInterface
public interface VariableSetter {

    /**
     * Update variable with value.
     *
     * @param attributePath contains path to attribute's value in scope of the station
     * @param attributeValue new value for variable
     */
    void set(AttributePath attributePath, CiString.CiString1000 attributeValue);
}
