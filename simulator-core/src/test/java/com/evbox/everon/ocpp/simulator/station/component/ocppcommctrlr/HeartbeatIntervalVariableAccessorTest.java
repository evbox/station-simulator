package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.evbox.everon.ocpp.simulator.assertion.CiStringAssert.assertCiString;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_HEARTBEAT_INTERVAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeartbeatIntervalVariableAccessorTest {

    @Mock
    Station stationMock;
    @Mock
    StationState stationStateMock;

    @InjectMocks
    HeartbeatIntervalVariableAccessor variableAccessor;

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(OCPPCommCtrlrComponent.NAME, HeartbeatIntervalVariableAccessor.NAME, SetVariableDatum.AttributeType.ACTUAL, DEFAULT_HEARTBEAT_INTERVAL, SetVariableResult.AttributeStatus.ACCEPTED),
                arguments(OCPPCommCtrlrComponent.NAME, HeartbeatIntervalVariableAccessor.NAME, SetVariableDatum.AttributeType.ACTUAL, -DEFAULT_HEARTBEAT_INTERVAL, SetVariableResult.AttributeStatus.INVALID_VALUE),
                arguments(OCPPCommCtrlrComponent.NAME, HeartbeatIntervalVariableAccessor.NAME, SetVariableDatum.AttributeType.MAX_SET, DEFAULT_HEARTBEAT_INTERVAL, SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        return Stream.of(
                arguments(OCPPCommCtrlrComponent.NAME, HeartbeatIntervalVariableAccessor.NAME, GetVariableDatum.AttributeType.ACTUAL, GetVariableResult.AttributeStatus.ACCEPTED, DEFAULT_HEARTBEAT_INTERVAL),
                arguments(OCPPCommCtrlrComponent.NAME, HeartbeatIntervalVariableAccessor.NAME, GetVariableDatum.AttributeType.MAX_SET, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null)
        );
    }

    @ParameterizedTest
    @MethodSource("setVariableDatumProvider")
    void shouldValidateSetVariableDatum(String componentName, String variableName, SetVariableDatum.AttributeType attributeType, int heartbeatInterval, SetVariableResult.AttributeStatus expectedAttributeStatus) {
        //when
        SetVariableResult result = variableAccessor.validate(
                new Component().withName(new CiString.CiString50(componentName)),
                new Variable().withName(new CiString.CiString50(variableName)),
                attributeType,
                new CiString.CiString1000(String.valueOf(heartbeatInterval))
        );

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(componentName);
        assertCiString(result.getVariable().getName()).isEqualTo(variableName);
        assertThat(result.getAttributeType()).isEqualTo(SetVariableResult.AttributeType.fromValue(attributeType.value()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
    }

    @ParameterizedTest
    @MethodSource("getVariableDatumProvider")
    void shouldGetVariableDatum(String componentName, String variableName, GetVariableDatum.AttributeType attributeType, GetVariableResult.AttributeStatus expectedAttributeStatus, Integer expectedValue) {
        //given
        if (expectedValue != null) {
            initStationMockHeartbeat(expectedValue);
        }

        //when
        GetVariableResult result = variableAccessor.get(
                new Component().withName(new CiString.CiString50(componentName)),
                new Variable().withName(new CiString.CiString50(variableName)),
                attributeType);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(componentName);
        assertCiString(result.getVariable().getName()).isEqualTo(variableName);
        assertThat(result.getAttributeType()).isEqualTo(GetVariableResult.AttributeType.fromValue(attributeType.value()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue == null ? null : String.valueOf(expectedValue));
    }

    @Test
    void shouldSetActualValue() {
        //given
        Component component = new Component().withName(new CiString.CiString50(OCPPCommCtrlrComponent.NAME));
        Variable variable = new Variable().withName(new CiString.CiString50(HeartbeatIntervalVariableAccessor.NAME));
        SetVariableDatum.AttributeType attributeType = SetVariableDatum.AttributeType.ACTUAL;
        int heartbeatInterval = 100;

        //when
        variableAccessor.setActualValue(component, variable, attributeType, new CiString.CiString1000(String.valueOf(heartbeatInterval)));

        //then
        verify(stationMock).updateHeartbeat(eq(heartbeatInterval));
    }

    private void initStationMockHeartbeat(Integer expectedValue) {
        given(stationMock.getState()).willReturn(stationStateMock);
        given(stationStateMock.getHeartbeatInterval()).willReturn(expectedValue);
    }

}