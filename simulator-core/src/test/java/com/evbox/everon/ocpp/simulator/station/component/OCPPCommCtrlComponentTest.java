package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.support.StationConstants;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.evbox.everon.ocpp.simulator.assertion.CiStringAssert.assertCiString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OCPPCommCtrlComponentTest {

    @Mock
    HeartbeatIntervalVariableAccessor heartbeatIntervalVariableAccessorMock;
    @Mock
    Station stationMock;
    @Captor
    ArgumentCaptor<CiString.CiString1000> ciStringCaptor;
    @Captor
    ArgumentCaptor<Component> componentCaptor;
    @Captor
    ArgumentCaptor<Variable> variableCaptor;
    @Captor
    ArgumentCaptor<SetVariableDatum.AttributeType> attributeTypeCaptor;

    OCPPCommCtrlrComponent ocppCommCtrlrComponent;

    @BeforeEach
    void setUp() throws Exception {
        ocppCommCtrlrComponent = new OCPPCommCtrlrComponent(stationMock);
        Map<String, VariableAccessor> variableAccessors
                = ImmutableMap.of(HeartbeatIntervalVariableAccessor.NAME, heartbeatIntervalVariableAccessorMock);

        FieldUtils.writeField(ocppCommCtrlrComponent, "variableAccessors", variableAccessors, true);
    }

    @Test
    void shouldReturnGetVariableResult() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        GetVariableDatum.AttributeType attributeType = GetVariableDatum.AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        GetVariableDatum getVariableDatum = new GetVariableDatum()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType);

        initGetHeartbeatVariableMock(componentName, variableName, attributeType, String.valueOf(variableValue));

        //when
        GetVariableResult result = ocppCommCtrlrComponent.getVariable(getVariableDatum);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(componentName);
        assertCiString(result.getVariable().getName()).isEqualTo(variableName);
        assertCiString(result.getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(result.getAttributeType()).isEqualTo(GetVariableResult.AttributeType.fromValue(attributeType.value()));
    }

    @Test
    void shouldReturnSetVariableResult() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        SetVariableDatum.AttributeType attributeType = SetVariableDatum.AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType)
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        ocppCommCtrlrComponent.setVariable(setVariableDatum);

        verify(heartbeatIntervalVariableAccessorMock).set(componentCaptor.capture(), variableCaptor.capture(), attributeTypeCaptor.capture(), ciStringCaptor.capture());

        //then
        assertCiString(componentCaptor.getValue().getName()).isEqualTo(componentName);
        assertCiString(variableCaptor.getValue().getName()).isEqualTo(variableName);
        assertCiString(ciStringCaptor.getValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(attributeTypeCaptor.getValue()).isEqualTo(attributeType);
    }

    @Test
    void shouldValidateSetVariableDatum() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        SetVariableDatum.AttributeType attributeType = SetVariableDatum.AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        given(heartbeatIntervalVariableAccessorMock.validate(any(Component.class), any(Variable.class), eq(attributeType), eq(new CiString.CiString1000(String.valueOf(variableValue)))))
                .willAnswer(invocation -> new SetVariableResult()
                        .withComponent(invocation.getArgument(0))
                        .withVariable(invocation.getArgument(1))
                        .withAttributeType(SetVariableResult.AttributeType.fromValue(((SetVariableDatum.AttributeType)invocation.getArgument(2)).value()))
                        .withAttributeStatus(SetVariableResult.AttributeStatus.ACCEPTED));

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType)
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        SetVariableValidationResult validationResult = ocppCommCtrlrComponent.validate(setVariableDatum);

        //then
        assertCiString(validationResult.getSetVariableDatum().getComponent().getName()).isEqualTo(componentName);
        assertCiString(validationResult.getSetVariableDatum().getVariable().getName()).isEqualTo(variableName);
        assertCiString(validationResult.getSetVariableDatum().getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(validationResult.getSetVariableDatum().getAttributeType()).isEqualTo(SetVariableDatum.AttributeType.ACTUAL);
        assertThat(validationResult.getResult().getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.ACCEPTED);
    }

    @Test
    void shouldValidateIfVariableExists() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = "UnknownVariableName";
        SetVariableDatum.AttributeType attributeType = SetVariableDatum.AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType)
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        SetVariableValidationResult validationResult = ocppCommCtrlrComponent.validate(setVariableDatum);

        //then
        assertCiString(validationResult.getSetVariableDatum().getComponent().getName()).isEqualTo(componentName);
        assertCiString(validationResult.getSetVariableDatum().getVariable().getName()).isEqualTo(variableName);
        assertCiString(validationResult.getSetVariableDatum().getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(validationResult.getSetVariableDatum().getAttributeType()).isEqualTo(SetVariableDatum.AttributeType.ACTUAL);
        assertThat(validationResult.getResult().getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.UNKNOWN_VARIABLE);
    }

    private void initGetHeartbeatVariableMock(String componentName, String variableName, GetVariableDatum.AttributeType attributeType, String returnValue) {
        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));
        CiString.CiString1000 attributeValue = new CiString.CiString1000(returnValue);
        GetVariableResult.AttributeType returnAttributeType = GetVariableResult.AttributeType.fromValue(attributeType.value());

        given(heartbeatIntervalVariableAccessorMock.get(any(Component.class), any(Variable.class), any(GetVariableDatum.AttributeType.class)))
                .willReturn(new GetVariableResult()
                        .withComponent(component)
                        .withVariable(variable)
                        .withAttributeType(returnAttributeType)
                        .withAttributeValue(attributeValue));
    }

}
