package com.evbox.everon.ocpp.simulator.station.component.securityctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BasicAuthPasswordVariableAccessor extends VariableAccessor {

    private static final String NAME = "BasicAuthPassword";

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::validateActualValue)
            .build();

    private final Map<AttributeType, VariableSetter> variableSetters = ImmutableMap.<AttributeType, VariableSetter>builder()
            .put(AttributeType.ACTUAL, this::setActualValue)
            .build();

    public BasicAuthPasswordVariableAccessor(Station station, StationState stationState) {
        super(station, stationState);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public Map<AttributeType, VariableGetter> getVariableGetters() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Map<AttributeType, VariableSetter> getVariableSetters() {
        return variableSetters;
    }

    @Override
    public Map<AttributeType, SetVariableValidator> getVariableValidators() {
        return variableValidators;
    }

    @Override
    public List<ReportDatum> generateReportData(String componentName) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(SetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));

        if (attributeValue.toString().length() > 40) {
            return setVariableResult.withAttributeStatus(SetVariableResult.AttributeStatus.INVALID_VALUE);
        } else {
            return setVariableResult.withAttributeStatus(SetVariableResult.AttributeStatus.ACCEPTED);
        }
    }

    private void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        getStation().getConfiguration().setPassword(attributeValue.toString());
        getStation().reconnect();
    }
}
