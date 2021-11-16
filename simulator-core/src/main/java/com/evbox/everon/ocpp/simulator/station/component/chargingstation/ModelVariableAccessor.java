package com.evbox.everon.ocpp.simulator.station.component.chargingstation;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.Component;
import com.evbox.everon.ocpp.v201.message.station.Variable;
import com.evbox.everon.ocpp.v201.message.station.*;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class ModelVariableAccessor extends VariableAccessor {

    public static final String NAME = "Model";
    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::rejectVariable)
            .build();

    public ModelVariableAccessor(Station station, StationStore stationStore) {
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
        return Collections.emptyMap();
    }

    @Override
    public Map<AttributeType, SetVariableValidator> getVariableValidators() {
        return variableValidators;
    }

    @Override
    public List<ReportData> generateReportData(String componentName) {
        Component component = new Component()
                .withName(new CiString.CiString50(componentName));

        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(getStationStore().getStationModel()))
                .withPersistent(true)
                .withConstant(true)
                .withMutability(Mutability.READ_ONLY);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(Data.STRING)
                .withSupportsMonitoring(false);

        ReportData reportDatum = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(reportDatum);
    }

    @Override
    public boolean isMutable() { return false; }

    protected SetVariableResult rejectVariable(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        return RESULT_CREATOR.createResult(attributePath, attributeValue, SetVariableStatus.REJECTED);
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        return new GetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(getStationStore().getStationModel()))
                .withAttributeStatus(GetVariableStatus.ACCEPTED);
    }
}
