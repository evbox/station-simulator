package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableNotSupportedException;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Represents Component entity from OCPP 2.0 (Appendix 3. Standardized Components).
 * Supports validation, update and retrieval logic for component variables.
 */
public abstract class StationComponent {

    /**
     * Map of variable names and accessors for each of them.
     */
    private final Map<String, VariableAccessor> variableAccessors;

    public StationComponent(List<VariableAccessor> variableAccessors) {
        this.variableAccessors = ImmutableMap.copyOf(variableAccessors.stream().collect(toMap(VariableAccessor::getVariableName, identity())));
    }

    public abstract String getComponentName();

    /**
     * Validates {@link GetVariableDatum} for proper variable path (variable name, instance, attributeType, evseId, connectorId) and read access.
     * Retrieves variable from station.
     *
     * @param getVariableDatum contains necessary data to get variable from station
     * @return result of getting variable
     */
    public GetVariableResult handle(GetVariableDatum getVariableDatum) {
        Component component = getVariableDatum.getComponent();
        GetVariableDatum.AttributeType attributeType = getVariableDatum.getAttributeType();
        Variable variable = getVariableDatum.getVariable();

        VariableAccessor accessor = variableAccessors.get(variable.getName().toString());

        return accessor.get(component, variable, attributeType);
    }

    /**
     * Updates variable.
     *
     * @param setVariableDatum contains path to variable and new value for it
     * @throws SetVariableNotSupportedException if variable specified in {@link SetVariableDatum} is not supported by component
     */
    public void handle(SetVariableDatum setVariableDatum) {
        Component component = setVariableDatum.getComponent();
        Variable variable = setVariableDatum.getVariable();
        String variableName = variable.getName().toString();

        VariableAccessor accessor = variableAccessors.get(variableName);

        accessor.set(component, variable, setVariableDatum.getAttributeType(), setVariableDatum.getAttributeValue());
    }

    /**
     * Validates {@link SetVariableDatum} for proper variable path (variable name, instance, attributeType, evseId, connectorId) and modification access.
     * Since station has to reply to SetVariablesRequest immediately, validation logic should happen before update's execution.
     * This is why validate stands as a separate operation.
     *
     * @param setVariableDatum contains path to variable and new value
     * @return result which contains status of variable modification
     */
    public SetVariableValidationResult validate(SetVariableDatum setVariableDatum) {
        Optional<VariableAccessor> optionalVariableAccessor = Optional.ofNullable(variableAccessors.get(setVariableDatum.getVariable().getName().toString()));

        SetVariableResult validationResult = optionalVariableAccessor
                .map(accessor -> accessor.validate(setVariableDatum.getComponent(), setVariableDatum.getVariable(), setVariableDatum.getAttributeType(), setVariableDatum.getAttributeValue()))
                .orElse(new SetVariableResult()
                        .withComponent(setVariableDatum.getComponent())
                        .withVariable(setVariableDatum.getVariable())
                        .withAttributeType(SetVariableResult.AttributeType.fromValue(setVariableDatum.getAttributeType().value()))
                        .withAttributeStatus(SetVariableResult.AttributeStatus.UNKNOWN_VARIABLE)
                );

        return new SetVariableValidationResult(setVariableDatum, validationResult);
    }

}
