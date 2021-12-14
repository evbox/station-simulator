package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
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

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
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
    @Mock
    StationStore stationStore;
    @Captor
    ArgumentCaptor<CiString.CiString1000> ciStringCaptor;
    @Captor
    ArgumentCaptor<AttributePath> attributePathCaptor;

    OCPPCommCtrlrComponent ocppCommCtrlrComponent;

    @BeforeEach
    void setUp() throws Exception {
        ocppCommCtrlrComponent = new OCPPCommCtrlrComponent(stationMock, stationStore);
        Map<CiString.CiString50, VariableAccessor> variableAccessors
                = ImmutableMap.of(new CiString.CiString50(ocppCommCtrlrComponent.getComponentName() + "-" + HeartbeatIntervalVariableAccessor.NAME), heartbeatIntervalVariableAccessorMock);

        FieldUtils.writeField(ocppCommCtrlrComponent, "variableAccessors", variableAccessors, true);
    }

    @Test
    void shouldReturnGetVariableResult() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        Attribute attributeType = Attribute.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        GetVariableData getVariableDatum = new GetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType);

        initGetHeartbeatVariableMock(new AttributePath(component, variable, attributeType), String.valueOf(variableValue));

        //when
        GetVariableResult result = ocppCommCtrlrComponent.getVariable(getVariableDatum);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(componentName);
        assertCiString(result.getVariable().getName()).isEqualTo(variableName);
        assertCiString(result.getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(result.getAttributeType()).isEqualTo(Attribute.fromValue(attributeType.value()));
    }

    @Test
    void shouldReturnSetVariableResult() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        AttributeType attributeType = AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableData setVariableDatum = new SetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(Attribute.fromValue(attributeType.getName()))
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        ocppCommCtrlrComponent.setVariable(setVariableDatum);

        verify(heartbeatIntervalVariableAccessorMock).set(attributePathCaptor.capture(), ciStringCaptor.capture());

        //then
        AttributePath actualAttributePath = attributePathCaptor.getValue();
        assertCiString(actualAttributePath.getComponent().getName()).isEqualTo(componentName);
        assertCiString(actualAttributePath.getVariable().getName()).isEqualTo(variableName);
        assertThat(actualAttributePath.getAttributeType()).isEqualTo(attributeType);
        assertCiString(ciStringCaptor.getValue()).isEqualTo(String.valueOf(variableValue));
    }

    @Test
    void shouldValidateSetVariableDatum() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        AttributeType attributeType = AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        given(heartbeatIntervalVariableAccessorMock.validate(any(AttributePath.class), eq(new CiString.CiString1000(String.valueOf(variableValue)))))
                .willAnswer(invocation -> new SetVariableResult()
                        .withComponent(((AttributePath)invocation.getArgument(0)).getComponent())
                        .withVariable(((AttributePath)invocation.getArgument(0)).getVariable())
                        .withAttributeType(Attribute.fromValue(((AttributePath)invocation.getArgument(0)).getAttributeType().getName()))
                        .withAttributeStatus(SetVariableStatus.ACCEPTED));

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableData setVariableDatum = new SetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(Attribute.fromValue(attributeType.getName()))
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        SetVariableValidationResult validationResult = ocppCommCtrlrComponent.validate(setVariableDatum);

        //then
        assertCiString(validationResult.getSetVariableData().getComponent().getName()).isEqualTo(componentName);
        assertCiString(validationResult.getSetVariableData().getVariable().getName()).isEqualTo(variableName);
        assertCiString(validationResult.getSetVariableData().getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(validationResult.getSetVariableData().getAttributeType()).isEqualTo(Attribute.ACTUAL);
        assertThat(validationResult.getResult().getAttributeStatus()).isEqualTo(SetVariableStatus.ACCEPTED);
    }

    @Test
    void shouldValidateIfVariableExists() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = "UnknownVariableName";
        Attribute attributeType = Attribute.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableData setVariableDatum = new SetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType)
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        SetVariableValidationResult validationResult = ocppCommCtrlrComponent.validate(setVariableDatum);

        //then
        assertCiString(validationResult.getSetVariableData().getComponent().getName()).isEqualTo(componentName);
        assertCiString(validationResult.getSetVariableData().getVariable().getName()).isEqualTo(variableName);
        assertCiString(validationResult.getSetVariableData().getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(validationResult.getSetVariableData().getAttributeType()).isEqualTo(Attribute.ACTUAL);
        assertThat(validationResult.getResult().getAttributeStatus()).isEqualTo(SetVariableStatus.UNKNOWN_VARIABLE);
    }

    private void initGetHeartbeatVariableMock(AttributePath attributePath, String returnValue) {
        CiString.CiString2500 attributeValue = new CiString.CiString2500(returnValue);

        given(heartbeatIntervalVariableAccessorMock.get(any(AttributePath.class)))
                .willReturn(new GetVariableResult()
                        .withComponent(attributePath.getComponent())
                        .withVariable(attributePath.getVariable())
                        .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                        .withAttributeValue(attributeValue));
    }

}
