package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public abstract class StationComponent implements GetVariableHandler, SetVariableHandler {

    private final Map<String, VariableAccessor> variableAccessors;

    public StationComponent(List<VariableAccessor> variableAccessors) {
        this.variableAccessors = ImmutableMap.copyOf(variableAccessors.stream().collect(toMap(VariableAccessor::getVariableName, identity())));
    }

    public abstract String getComponentName();

    public GetVariableResult handle(GetVariableDatum getVariableDatum) {
        Component component = getVariableDatum.getComponent();
        Evse evse = component.getEvse();
        GetVariableDatum.AttributeType attributeType = getVariableDatum.getAttributeType();
        Variable variable = getVariableDatum.getVariable();

        VariableAccessor accessor = variableAccessors.get(component.getName().toString());

        return accessor.get(component, evse, variable, attributeType);
    }

    public void handle(SetVariableDatum setVariableDatum) {
        Component component = setVariableDatum.getComponent();
        Evse evse = component.getEvse();
        Variable variable = setVariableDatum.getVariable();
        String variableName = variable.getName().toString();

        VariableAccessor accessor = variableAccessors.get(variableName);

        accessor.set(component, evse, variable, setVariableDatum.getAttributeType(), setVariableDatum.getAttributeValue());
    }

    public SetVariableValidationResult validate(SetVariableDatum setVariableDatum) {
        Optional<VariableAccessor> optionalVariableAccessor = Optional.ofNullable(variableAccessors.get(setVariableDatum.getVariable().getName().toString()));

        SetVariableResult.AttributeStatus status = optionalVariableAccessor
                .map(accessor -> accessor.validate(setVariableDatum.getAttributeType(), setVariableDatum.getAttributeValue()))
                .orElse(SetVariableResult.AttributeStatus.UNKNOWN_VARIABLE);

        return new SetVariableValidationResult(setVariableDatum, status);
    }

}
