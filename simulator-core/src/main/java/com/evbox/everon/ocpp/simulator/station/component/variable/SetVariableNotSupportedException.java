package com.evbox.everon.ocpp.simulator.station.component.variable;

public class SetVariableNotSupportedException extends RuntimeException {

    public SetVariableNotSupportedException(String componentName, String variableName, String attributeType) {
        super(String.format("Set variable is not supported for: component='%s', variable='%s', attributeType='%s'", componentName, variableName, attributeType));
    }
}
