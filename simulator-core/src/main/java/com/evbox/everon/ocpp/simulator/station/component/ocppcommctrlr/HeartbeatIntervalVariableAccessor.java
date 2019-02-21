package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class HeartbeatIntervalVariableAccessor extends VariableAccessor {

    public static final String NAME = "HeartbeatInterval";
    private final Map<GetVariableDatum.AttributeType, VariableGetter> variableGetters = ImmutableMap.<GetVariableDatum.AttributeType, VariableGetter>builder()
            .put(GetVariableDatum.AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<SetVariableDatum.AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<SetVariableDatum.AttributeType, SetVariableValidator>builder()
            .put(SetVariableDatum.AttributeType.ACTUAL, this::validateActualValue)
            .build();

    private final Map<SetVariableDatum.AttributeType, VariableSetter> variableSetters = ImmutableMap.<SetVariableDatum.AttributeType, VariableSetter>builder()
            .put(SetVariableDatum.AttributeType.ACTUAL, this::setActualValue)
            .build();

    public HeartbeatIntervalVariableAccessor(Station station) {
        super(station);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public Map<GetVariableDatum.AttributeType, VariableGetter> getVariableGetters() {
        return variableGetters;
    }

    @Override
    public Map<SetVariableDatum.AttributeType, VariableSetter> getVariableSetters() {
        return variableSetters;
    }

    @Override
    public Map<SetVariableDatum.AttributeType, SetVariableValidator> getVariableValidators() {
        return variableValidators;
    }

    private SetVariableResult validateActualValue(Component component, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(SetVariableResult.AttributeType.fromValue(attributeType.value()));

        if (!isNumeric(attributeValue.toString())) {
            setVariableResult = setVariableResult.withAttributeStatus(SetVariableResult.AttributeStatus.INVALID_VALUE);
        } else {
            setVariableResult = setVariableResult.withAttributeStatus(SetVariableResult.AttributeStatus.ACCEPTED);
        }

        return setVariableResult;
    }

    public void setActualValue(Component component, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue) {
        Station station = getStation();
        station.updateHeartbeat(Integer.parseInt(attributeValue.toString()));
    }

    private GetVariableResult getActualValue(Component component, Variable variable, GetVariableDatum.AttributeType attributeType) {
        Station station = getStation();
        int heartbeatInterval = station.getState().getHeartbeatInterval();

        return new GetVariableResult()
                .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED)
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(GetVariableResult.AttributeType.fromValue(attributeType.value()))
                .withAttributeValue(new CiString.CiString1000(String.valueOf(heartbeatInterval)));
    }
}
