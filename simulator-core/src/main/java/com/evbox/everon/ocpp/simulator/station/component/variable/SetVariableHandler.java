package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;

@FunctionalInterface
public interface SetVariableHandler {

    /**
     * Updates variable.
     *
     * @param setVariableDatum contains path to variable and new value for it
     * @throws SetVariableNotSupportedException if variable specified in {@link SetVariableDatum} is not supported by component
     */
    void setVariable(SetVariableDatum setVariableDatum);

}
