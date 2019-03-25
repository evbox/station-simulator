package com.evbox.everon.ocpp.simulator.station.component.evse;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
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

import static com.evbox.everon.ocpp.v20.message.station.VariableCharacteristics.DataType.BOOLEAN;
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

    public EnabledVariableAccessor(Station station) {
        super(station);
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

        for (Evse evse : getStation().getState().getEvses()) {
            com.evbox.everon.ocpp.v20.message.common.Evse componentEvse = new com.evbox.everon.ocpp.v20.message.common.Evse()
                    .withId(evse.getId());

            Component component = new Component()
                    .withName(new CiString.CiString50(componentName))
                    .withEvse(componentEvse);

            VariableAttribute variableAttribute = new VariableAttribute()
                    .withValue(new CiString.CiString1000(EVSE_ENABLED_STATUS));

            ReportDatum reportDatum = new ReportDatum()
                    .withComponent(component)
                    .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                    .withVariableCharacteristics(new VariableCharacteristics().withDataType(BOOLEAN))
                    .withVariableAttribute(singletonList(variableAttribute));

            reportData.add(reportDatum);
        }

        return reportData;
    }

    @Override
    public boolean isMutable() { return false; }

    private SetVariableResult rejectVariable(AttributePath attributePath, CiString.CiString1000 ciString1000) {
        return new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(SetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeStatus(SetVariableResult.AttributeStatus.REJECTED);
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        Integer evseId = attributePath.getComponent().getEvse().getId();

        GetVariableResult getVariableResult = new GetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(GetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));

        boolean evseExists = getStation().getState().hasEvse(evseId);

        if (evseExists) {
            return getVariableResult
                    .withAttributeValue(new CiString.CiString1000(EVSE_ENABLED_STATUS))
                    .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED);
        } else {
            return getVariableResult.withAttributeStatus(GetVariableResult.AttributeStatus.REJECTED);
        }
    }
}
