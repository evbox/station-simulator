package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.v20.message.centralserver.*;

import java.util.Map;
import java.util.Optional;

/**
 * Contains logic for variable read, write and validation of these operations.
 * Since station has to reply to SetVariablesRequest immediately, validation logic should happen before update's execution. This is why validate stands as a separate operation.
 */
public abstract class VariableAccessor implements VariableGetter, VariableSetter, SetVariableValidator {

    private static final String NULL_STR = "null";

    private static final VariableGetter NOT_SUPPORTED_ATTRIBUTE_TYPE_GETTER = (component, variable, attributeType) ->
            new GetVariableResult()
                    .withComponent(component)
                    .withVariable(variable)
                    .withAttributeType(GetVariableResult.AttributeType.fromValue(attributeType.value()))
                    .withAttributeStatus(GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE);

    private static final SetVariableValidator NOT_SUPPORTED_ATTRIBUTE_TYPE_VALIDATOR = (component, variable, attributeType, attributeValue) ->
            new SetVariableResult()
                    .withComponent(component)
                    .withVariable(variable)
                    .withAttributeType(SetVariableResult.AttributeType.fromValue(attributeType.value()))
                    .withAttributeStatus(SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE);

    protected static final SetVariableValidator READ_ONLY_VALIDATOR = (component, variable, attributeType, attributeValue) ->
            new SetVariableResult()
                    .withComponent(component)
                    .withVariable(variable)
                    .withAttributeType(SetVariableResult.AttributeType.fromValue(attributeType.value()))
                    .withAttributeStatus(SetVariableResult.AttributeStatus.REJECTED);

    private final Station station;

    public VariableAccessor(Station station) {
        this.station = station;
    }

    public Station getStation() {
        return station;
    }

    public abstract String getVariableName();

    public abstract Map<GetVariableDatum.AttributeType, VariableGetter> getVariableGetters();

    public abstract Map<SetVariableDatum.AttributeType, VariableSetter> getVariableSetters();

    public abstract Map<SetVariableDatum.AttributeType, SetVariableValidator> getVariableValidators();

    @Override
    public GetVariableResult get(Component component, Variable variable, GetVariableDatum.AttributeType attributeType) {
        return getVariableGetters().getOrDefault(attributeType, NOT_SUPPORTED_ATTRIBUTE_TYPE_GETTER)
                .get(component, variable, attributeType);
    }

    public SetVariableResult validate(Component component, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue) {
        return getVariableValidators().getOrDefault(attributeType, NOT_SUPPORTED_ATTRIBUTE_TYPE_VALIDATOR)
                .validate(component, variable, attributeType, attributeValue);
    }

    @Override
    public void set(Component component, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue) {
        String componentName = Optional.ofNullable(component).map(Component::getName).map(CiString::toString).orElse(NULL_STR);
        String variableName = Optional.ofNullable(variable).map(Variable::getName).map(CiString::toString).orElse(NULL_STR);
        String attributeTypeName = Optional.ofNullable(attributeType).map(SetVariableDatum.AttributeType::toString).orElse(NULL_STR);

        Optional.ofNullable(getVariableSetters().get(attributeType))
                .orElseThrow(() -> new SetVariableNotSupportedException(componentName, variableName, attributeTypeName))
                .set(component, variable, attributeType, attributeValue);
    }
}
