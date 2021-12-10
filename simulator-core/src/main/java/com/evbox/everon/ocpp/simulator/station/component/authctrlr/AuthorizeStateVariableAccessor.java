package com.evbox.everon.ocpp.simulator.station.component.authctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import com.evbox.everon.ocpp.v201.message.station.VariableAttribute;
import com.evbox.everon.ocpp.v201.message.station.VariableCharacteristics;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v201.message.station.Data.BOOLEAN;
import static java.util.Collections.singletonList;

public class AuthorizeStateVariableAccessor extends VariableAccessor {

    public static final String NAME = "Enabled";
    private final Map<AttributeType, VariableGetter> variableGetters = Map.of(AttributeType.ACTUAL, this::getActualValue);

    private final Map<AttributeType, SetVariableValidator> variableValidators = Map.of(AttributeType.ACTUAL, this::validateActualValue);

    private final Map<AttributeType, VariableSetter> variableSetters = Map.of(AttributeType.ACTUAL, this::setActualValue);

    public AuthorizeStateVariableAccessor(Station station, StationStore stationStore) {
        super(station, stationStore);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public Map<AttributeType, VariableGetter> getVariableGetters() {
        return variableGetters;
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
    public List<ReportData> generateReportData(String componentName) {
        com.evbox.everon.ocpp.v201.message.station.Component component = new com.evbox.everon.ocpp.v201.message.station.Component()
                .withName(new CiString.CiString50(componentName));

        boolean authEnabled = getStationStore().isAuthEnabled();
        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(String.valueOf(authEnabled)))
                .withPersistent(false)
                .withConstant(false);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(BOOLEAN)
                .withSupportsMonitoring(false);

        ReportData reportDatum = new ReportData()
                .withComponent(component)
                .withVariable(new com.evbox.everon.ocpp.v201.message.station.Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(reportDatum);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()));
        String value = attributeValue.toString();
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return setVariableResult.withAttributeStatus(SetVariableStatus.ACCEPTED);
        } else {
            return setVariableResult.withAttributeStatus(SetVariableStatus.REJECTED);
        }
    }

    public void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        Station station = getStation();
        station.setAuthorizeState(Boolean.parseBoolean(attributeValue.toString()));
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        boolean authEnabled = getStationStore().isAuthEnabled();

        return new GetVariableResult()
                .withAttributeStatus(GetVariableStatus.ACCEPTED)
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(String.valueOf(authEnabled)));
    }
}
