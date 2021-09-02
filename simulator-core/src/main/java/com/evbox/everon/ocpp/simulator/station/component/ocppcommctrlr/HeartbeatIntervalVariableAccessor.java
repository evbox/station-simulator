package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

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
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.station.*;
import com.evbox.everon.ocpp.v201.message.station.Component;
import com.evbox.everon.ocpp.v201.message.station.Variable;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v201.message.station.Data.INTEGER;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class HeartbeatIntervalVariableAccessor extends VariableAccessor {

    public static final String NAME = "HeartbeatInterval";
    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::validateActualValue)
            .build();

    private final Map<AttributeType, VariableSetter> variableSetters = ImmutableMap.<AttributeType, VariableSetter>builder()
            .put(AttributeType.ACTUAL, this::setActualValue)
            .build();

    public HeartbeatIntervalVariableAccessor(Station station, StationStore stationStore) {
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
        Component component = new Component()
                .withName(new CiString.CiString50(componentName));

        int heartbeatInterval = getStationStore().getHeartbeatInterval();
        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(String.valueOf(heartbeatInterval)))
                .withPersistent(false)
                .withConstant(false);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(INTEGER)
                .withSupportsMonitoring(false);

        ReportData reportDatum = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(reportDatum);
    }

    @Override
    public boolean isMutable() { return true; }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()));

        if (!isNumeric(attributeValue.toString())) {
            return setVariableResult.withAttributeStatus(SetVariableStatus.REJECTED);
        } else {
            return setVariableResult.withAttributeStatus(SetVariableStatus.ACCEPTED);
        }
    }

    public void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        Station station = getStation();
        station.updateHeartbeat(Integer.parseInt(attributeValue.toString()));
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        int heartbeatInterval = getStationStore().getHeartbeatInterval();

        return new GetVariableResult()
                .withAttributeStatus(GetVariableStatus.ACCEPTED)
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(String.valueOf(heartbeatInterval)));
    }
}
