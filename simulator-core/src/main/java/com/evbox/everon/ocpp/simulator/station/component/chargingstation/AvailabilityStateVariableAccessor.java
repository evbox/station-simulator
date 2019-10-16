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
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import com.evbox.everon.ocpp.v20.message.station.VariableAttribute;
import com.evbox.everon.ocpp.v20.message.station.VariableCharacteristics;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v20.message.station.VariableAttribute.Mutability.READ_ONLY;
import static com.evbox.everon.ocpp.v20.message.station.VariableCharacteristics.DataType.SEQUENCE_LIST;
import static java.util.Collections.singletonList;

public class AvailabilityStateVariableAccessor extends VariableAccessor {

    public static final String NAME = "AvailabilityState";
    public static final String STATION_AVAILABILITY = "Available";

    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::rejectVariable)
            .build();

    public AvailabilityStateVariableAccessor(Station station, StationStore stationStore) {
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
    public List<ReportDatum> generateReportData(String componentName) {
        List<ReportDatum> reportData = new ArrayList<>();
        Component component = new Component().withName(new CiString.CiString50(componentName));

        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString1000(STATION_AVAILABILITY))
                .withPersistence(true)
                .withConstant(true)
                .withMutability(READ_ONLY);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(SEQUENCE_LIST)
                .withSupportsMonitoring(false);

        ReportDatum reportDatum = new ReportDatum()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        reportData.add(reportDatum);

        return reportData;
    }

    @Override
    public boolean isMutable() { return false; }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        Integer evseId = attributePath.getComponent().getEvse().getId();

        GetVariableResult getVariableResult = new GetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(GetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));

        boolean evseExists = getStationStore().hasEvse(evseId);

        if (evseExists) {
            return getVariableResult
                    .withAttributeValue(new CiString.CiString1000(STATION_AVAILABILITY))
                    .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED);
        } else {
            return getVariableResult.withAttributeStatus(GetVariableResult.AttributeStatus.REJECTED);
        }
    }

    private SetVariableResult rejectVariable(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        return RESULT_CREATOR.createResult(attributePath, attributeValue, SetVariableResult.AttributeStatus.REJECTED);
    }
}