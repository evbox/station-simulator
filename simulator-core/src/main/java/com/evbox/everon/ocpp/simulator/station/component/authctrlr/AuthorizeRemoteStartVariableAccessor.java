package com.evbox.everon.ocpp.simulator.station.component.authctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.ReportDataGenereator;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v201.message.centralserver.GetVariableStatus;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import com.evbox.everon.ocpp.v201.message.station.VariableAttribute;
import com.evbox.everon.ocpp.v201.message.station.VariableCharacteristics;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v201.message.station.Data.BOOLEAN;
import static com.evbox.everon.ocpp.v201.message.station.Data.INTEGER;
import static com.evbox.everon.ocpp.v201.message.station.Mutability.READ_WRITE;

public class AuthorizeRemoteStartVariableAccessor extends VariableAccessor {
    public static final String NAME = "AuthorizeRemoteStart";
    public static final String AUTH_REMOTE_START_ENABLED = "true";

    private final Map<AttributeType, VariableGetter> variableGetters = Map.of(AttributeType.ACTUAL, this::getActualValue);

    private final Map<AttributeType, SetVariableValidator> variableValidators = Map.of(AttributeType.ACTUAL, this::rejectVariable);

    public AuthorizeRemoteStartVariableAccessor(Station station, StationStore stationStore) {
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
        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(BOOLEAN)
                .withSupportsMonitoring(false);
        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(AUTH_REMOTE_START_ENABLED))
                .withPersistent(true)
                .withMutability(READ_WRITE)
                .withConstant(false);
        return ReportDataGenereator.generateReportData(componentName, variableCharacteristics, variableAttribute, NAME);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        return new GetVariableResult()
                .withAttributeStatus(GetVariableStatus.ACCEPTED)
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(AUTH_REMOTE_START_ENABLED));
    }
}
