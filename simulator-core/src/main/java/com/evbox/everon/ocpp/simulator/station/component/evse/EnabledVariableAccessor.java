package com.evbox.everon.ocpp.simulator.station.component.evse;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.station.*;
import com.evbox.everon.ocpp.v201.message.station.Component;
import com.evbox.everon.ocpp.v201.message.station.EVSE;
import com.evbox.everon.ocpp.v201.message.station.Variable;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class EnabledVariableAccessor extends VariableAccessor {

    public static final String NAME = "Enabled";
    public static final String EVSE_ENABLED_STATUS = "true";

    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::rejectVariable)
            .build();

    public EnabledVariableAccessor(Station station, StationStore stationStore) {
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
        List<ReportData> reportData = new ArrayList<>();

        for (Evse evse : getStationStore().getEvses()) {
            EVSE componentEvse = new EVSE()
                    .withId(evse.getId());

            Component component = new Component()
                    .withName(new CiString.CiString50(componentName))
                    .withEvse(componentEvse);

            VariableAttribute variableAttribute = new VariableAttribute()
                    .withValue(new CiString.CiString2500(EVSE_ENABLED_STATUS))
                    .withPersistent(true)
                    .withConstant(true)
                    .withMutability(Mutability.READ_ONLY);

            VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                    .withDataType(Data.BOOLEAN)
                    .withSupportsMonitoring(false);

            ReportData reportDatum = new ReportData()
                    .withComponent(component)
                    .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                    .withVariableCharacteristics(variableCharacteristics)
                    .withVariableAttribute(singletonList(variableAttribute));

            reportData.add(reportDatum);
        }

        return reportData;
    }

    @Override
    public boolean isMutable() { return false; }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        Integer evseId = attributePath.getComponent().getEvse().getId();

        GetVariableResult getVariableResult = new GetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()));

        boolean evseExists = getStationStore().hasEvse(evseId);

        if (evseExists) {
            return getVariableResult
                    .withAttributeValue(new CiString.CiString2500(EVSE_ENABLED_STATUS))
                    .withAttributeStatus(GetVariableStatus.ACCEPTED);
        } else {
            return getVariableResult.withAttributeStatus(GetVariableStatus.REJECTED);
        }
    }
}
