package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableNotSupportedException;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Represents Component entity from OCPP 2.0 (Appendix 3. Standardized Components).
 * Supports validation, update, report generation and retrieval logic for component variables.
 */
public abstract class StationComponent {

    private static final GetVariableResult UNKNOWN_VARIABLE = new GetVariableResult().withAttributeStatus(GetVariableResult.AttributeStatus.UNKNOWN_VARIABLE);

    /**
     * Map of variable names and accessors for each of them.
     */
    private final Map<String, VariableAccessor> variableAccessors;

    public StationComponent(List<VariableAccessor> variableAccessors) {
        this.variableAccessors = variableAccessors.stream()
                .collect(toMap(VariableAccessor::getVariableName,
                        identity(),
                        (e1, e2) -> e1,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
    }

    public abstract String getComponentName();

    /**
     * Validates {@link GetVariableDatum} for proper variable path (variable name, instance, attributeType, evseId, connectorId) and read access.
     * Retrieves variable from station.
     *
     * @param getVariableDatum contains necessary data to generate variable from station
     * @return result of getting variable
     */
    public GetVariableResult getVariable(GetVariableDatum getVariableDatum) {
        Component component = getVariableDatum.getComponent();
        GetVariableDatum.AttributeType attributeType = getVariableDatum.getAttributeType();
        Variable variable = getVariableDatum.getVariable();

        VariableAccessor accessor = variableAccessors.get(variable.getName().toString());

        if (isNull(accessor)) {
            return UNKNOWN_VARIABLE;
        }

        return accessor.get(new AttributePath(component, variable, attributeType));
    }

    /**
     * Updates variable.
     *
     * @param setVariableDatum contains path to variable and new value for it
     * @throws SetVariableNotSupportedException if variable specified in {@link SetVariableDatum} is not supported by component
     */
    public void setVariable(SetVariableDatum setVariableDatum) {
        Component component = setVariableDatum.getComponent();
        Variable variable = setVariableDatum.getVariable();
        String variableName = variable.getName().toString();

        VariableAccessor accessor = variableAccessors.get(variableName);

        accessor.set(new AttributePath(component, variable, setVariableDatum.getAttributeType()), setVariableDatum.getAttributeValue());
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
                .map(accessor -> accessor.validate(new AttributePath(setVariableDatum.getComponent(), setVariableDatum.getVariable(), setVariableDatum.getAttributeType()), setVariableDatum.getAttributeValue()))
                .orElse(new SetVariableResult()
                        .withComponent(setVariableDatum.getComponent())
                        .withVariable(setVariableDatum.getVariable())
                        .withAttributeType(SetVariableResult.AttributeType.fromValue(setVariableDatum.getAttributeType().value()))
                        .withAttributeStatus(SetVariableResult.AttributeStatus.UNKNOWN_VARIABLE)
                );

        return new SetVariableValidationResult(setVariableDatum, validationResult);
    }

    /**
     * Generates report data for all variables in the component
     *
     * @param onlyMutableVariables if true, returns only those variables that can be set by the operator
     * @return list of {@link ReportDatum}
     */
    public List<ReportDatum> generateReportData(boolean onlyMutableVariables) {
        List<ReportDatum> reportData = new ArrayList<>();

        variableAccessors.values().forEach(accessor -> {
            if (shouldGenerateReportDataForVariable(onlyMutableVariables, accessor.isMutable())) {
                reportData.addAll(accessor.generateReportData(getComponentName()));
            }
        });

        return reportData;
    }

    // Generate report datum if
    // 1) We are generating report datum for all variable OR
    // 2) The variable is mutable
    private boolean shouldGenerateReportDataForVariable(boolean onlyMutableVariables, boolean isMutableVariable) {
        return !onlyMutableVariables || isMutableVariable;
    }
}
