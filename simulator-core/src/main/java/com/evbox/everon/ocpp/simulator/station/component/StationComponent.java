package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableNotSupportedException;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Represents Component entity from OCPP 2.0 (Appendix 3. Standardized Components).
 * Supports validation, update, report generation and retrieval logic for component variables.
 */
public abstract class StationComponent {

    private static final GetVariableResult UNKNOWN_VARIABLE = new GetVariableResult().withAttributeStatus(GetVariableStatus.UNKNOWN_VARIABLE);

    /**
     * Map of variable names and accessors for each of them.
     */
    private final Map<CiString.CiString50, VariableAccessor> variableAccessors;

    public StationComponent(List<VariableAccessor> variableAccessors) {
        this.variableAccessors = ImmutableMap.copyOf(variableAccessors.stream().collect(
                toMap(va -> new CiString.CiString50(getComponentName() + "-" + va.getVariableName()), identity())));
    }

    public abstract String getComponentName();

    /**
     * Validates {@link GetVariableData} for proper variable path (variable name, instance, attributeType, evseId, connectorId) and read access.
     * Retrieves variable from station.
     *
     * @param getVariableDatum contains necessary data to generate variable from station
     * @return result of getting variable
     */
    public GetVariableResult getVariable(GetVariableData getVariableDatum) {
        Component component = getVariableDatum.getComponent();
        Attribute attributeType = getVariableDatum.getAttributeType();
        Variable variable = getVariableDatum.getVariable();

        VariableAccessor accessor = variableAccessors.get(new CiString.CiString50(getComponentName() + "-" + variable.getName()));

        if (isNull(accessor)) {
            return UNKNOWN_VARIABLE;
        }

        return accessor.get(new AttributePath(component, variable, attributeType));
    }

    /**
     *  Retrieves the name of the variables for this specific component.
     *
     * @return Set with name of variables
     */
    public Set<String> getVariableNames() {
        return variableAccessors.keySet().stream().map(CiString::toString).map(key -> key.split("-")[0]).collect(Collectors.toSet());
    }

    /**
     * Updates variable.
     *
     * @param setVariableData contains path to variable and new value for it
     * @throws SetVariableNotSupportedException if variable specified in {@link SetVariableData} is not supported by component
     */
    public void setVariable(SetVariableData setVariableData) {
        Component component = setVariableData.getComponent();
        Variable variable = setVariableData.getVariable();

        VariableAccessor accessor = variableAccessors.get(new CiString.CiString50(getComponentName() + "-" + variable.getName()));

        accessor.set(new AttributePath(component, variable, setVariableData.getAttributeType()), setVariableData.getAttributeValue());
    }

    /**
     * Validates {@link SetVariableData} for proper variable path (variable name, instance, attributeType, evseId, connectorId) and modification access.
     * Since station has to reply to SetVariablesRequest immediately, validation logic should happen before update's execution.
     * This is why validate stands as a separate operation.
     *
     * @param setVariableData contains path to variable and new value
     * @return result which contains status of variable modification
     */
    public SetVariableValidationResult validate(SetVariableData setVariableData) {
        Optional<VariableAccessor> optionalVariableAccessor = Optional.ofNullable(variableAccessors.get(new CiString.CiString50(getComponentName() + "-" + setVariableData.getVariable().getName())));

        SetVariableResult validationResult = optionalVariableAccessor
                .map(accessor -> accessor.validate(new AttributePath(setVariableData.getComponent(), setVariableData.getVariable(), setVariableData.getAttributeType()), setVariableData.getAttributeValue()))
                .orElse(new SetVariableResult()
                        .withComponent(setVariableData.getComponent())
                        .withVariable(setVariableData.getVariable())
                        .withAttributeType(Attribute.fromValue(setVariableData.getAttributeType().value()))
                        .withAttributeStatus(SetVariableStatus.UNKNOWN_VARIABLE)
                );

        return new SetVariableValidationResult(setVariableData, validationResult);
    }

    /**
     * Retrieves the variable accessor by the name specified.
     *
     * @param name name of the Variable
     * @return requested variable accessor
     */
    public VariableAccessor getVariableAccessorByName(CiString.CiString50 name) {
        return variableAccessors.get(getComponentName() + "-" + name);
    }

    /**
     * Generates report data for all variables in the component
     *
     * @param onlyMutableVariables if true, returns only those variables that can be set by the operator
     * @return list of {@link ReportData}
     */
    public List<ReportData> generateReportData(boolean onlyMutableVariables) {
        List<ReportData> reportData = new ArrayList<>();

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
