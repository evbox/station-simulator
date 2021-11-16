package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.ReportData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contains logic for variable read, write and validation of these operations.
 * Since station has to reply to SetVariablesRequest immediately, validation logic should happen before update's execution.
 * This is why validate stands as a separate operation.
 */
public abstract class VariableAccessor implements VariableGetter, VariableSetter, SetVariableValidator {

    private static final String NULL_STR = "null";

    private static final VariableGetter NOT_SUPPORTED_ATTRIBUTE_TYPE_GETTER = (attributePath) ->
            new GetVariableResult()
                    .withComponent(attributePath.getComponent())
                    .withVariable(attributePath.getVariable())
                    .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                    .withAttributeStatus(GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE);

    private static final SetVariableValidator NOT_SUPPORTED_ATTRIBUTE_TYPE_VALIDATOR = (attributePath, attributeValue) ->
            new SetVariableResult()
                    .withComponent(attributePath.getComponent())
                    .withVariable(attributePath.getVariable())
                    .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                    .withAttributeStatus(SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE);

    protected static final SetVariableResultCreator RESULT_CREATOR = (attributePath, attributeValue, attributeStatus) ->
            new SetVariableResult()
                    .withComponent(attributePath.getComponent())
                    .withVariable(attributePath.getVariable())
                    .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                    .withAttributeStatus(attributeStatus);

    private final Station station;
    private final StationStore stationStore;

    public VariableAccessor(Station station, StationStore stationStore) {
        this.station = station;
        this.stationStore = stationStore;
    }

    public Station getStation() {
        return station;
    }

    public StationStore getStationStore() {
        return stationStore;
    }

    public abstract String getVariableName();

    public abstract Map<AttributeType, VariableGetter> getVariableGetters();

    public abstract Map<AttributeType, VariableSetter> getVariableSetters();

    public abstract Map<AttributeType, SetVariableValidator> getVariableValidators();

    public abstract List<ReportData> generateReportData(String componentName);

    public abstract boolean isMutable();

    @Override
    public GetVariableResult get(AttributePath attributePath) {
        return getVariableGetters().getOrDefault(attributePath.getAttributeType(), NOT_SUPPORTED_ATTRIBUTE_TYPE_GETTER)
                .get(attributePath);
    }

    public SetVariableResult validate(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        return getVariableValidators().getOrDefault(attributePath.getAttributeType(), NOT_SUPPORTED_ATTRIBUTE_TYPE_VALIDATOR)
                .validate(attributePath, attributeValue);
    }

    @Override
    public void set(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        String componentName = Optional.ofNullable(attributePath.getComponent()).map(Component::getName).map(CiString::toString).orElse(NULL_STR);
        String variableName = Optional.ofNullable(attributePath.getVariable()).map(Variable::getName).map(CiString::toString).orElse(NULL_STR);
        String attributeTypeName = Optional.ofNullable(attributePath.getAttributeType()).map(AttributeType::getName).orElse(NULL_STR);

        Optional.ofNullable(getVariableSetters().get(attributePath.getAttributeType()))
                .orElseThrow(() -> new SetVariableNotSupportedException(componentName, variableName, attributeTypeName))
                .set(attributePath, attributeValue);
    }

    protected SetVariableResult rejectVariable(AttributePath attributePath, CiString.CiString1000 ciString1000) {
        return new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeStatus(SetVariableStatus.REJECTED);
    }
}
