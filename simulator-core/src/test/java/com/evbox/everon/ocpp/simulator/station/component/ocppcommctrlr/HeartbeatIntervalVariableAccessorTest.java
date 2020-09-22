package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_HEARTBEAT_INTERVAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeartbeatIntervalVariableAccessorTest {

    private static final String COMPONENT_NAME = OCPPCommCtrlrComponent.NAME;
    private static final String VARIABLE_NAME = HeartbeatIntervalVariableAccessor.NAME;

    private static final AttributePath ACTUAL_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.ACTUAL).build();
    private static final AttributePath MAX_SET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.MAX_SET).build();
    private static final AttributePath MIN_SET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.MIN_SET).build();
    private static final AttributePath TARGET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.TARGET).build();

    @Mock(lenient = true)
    Station stationMock;
    @Mock(lenient = true)
    StationStore stationStoreMock;

    @InjectMocks
    HeartbeatIntervalVariableAccessor variableAccessor;

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, DEFAULT_HEARTBEAT_INTERVAL, SetVariableStatus.ACCEPTED),
                arguments(ACTUAL_ATTRIBUTE, -DEFAULT_HEARTBEAT_INTERVAL, SetVariableStatus.REJECTED), //TODO check that this replaces INVALID_VALUE from OCPP 2.0 to OCPP 2.0.1
                arguments(MAX_SET_ATTRIBUTE, DEFAULT_HEARTBEAT_INTERVAL, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(MIN_SET_ATTRIBUTE, DEFAULT_HEARTBEAT_INTERVAL, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(TARGET_ATTRIBUTE, DEFAULT_HEARTBEAT_INTERVAL, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, GetVariableStatus.ACCEPTED, String.valueOf(DEFAULT_HEARTBEAT_INTERVAL)),
                arguments(MAX_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(MIN_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(TARGET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null)
        );
    }

    @ParameterizedTest
    @MethodSource("setVariableDatumProvider")
    void shouldValidateSetVariableDatum(AttributePath attributePath, int heartbeatInterval, SetVariableStatus expectedAttributeStatus) {
        //when
        SetVariableResult result = variableAccessor.validate(attributePath, new CiString.CiString1000(String.valueOf(heartbeatInterval)));

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(Attribute.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
    }

    @ParameterizedTest
    @MethodSource("getVariableDatumProvider")
    void shouldGetVariableDatum(AttributePath attributePath, GetVariableStatus expectedAttributeStatus, String expectedValue) {
        //given
        initStationMockHeartbeat();

        //when
        GetVariableResult result = variableAccessor.get(attributePath);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(Attribute.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }

    @Test
    void shouldSetActualValue() {
        //given
        Component component = new Component().withName(new CiString.CiString50(OCPPCommCtrlrComponent.NAME));
        Variable variable = new Variable().withName(new CiString.CiString50(HeartbeatIntervalVariableAccessor.NAME));
        Attribute attributeType = Attribute.ACTUAL;
        int heartbeatInterval = 100;

        //when
        variableAccessor.setActualValue(new AttributePath(component, variable, attributeType), new CiString.CiString1000(String.valueOf(heartbeatInterval)));

        //then
        verify(stationMock).updateHeartbeat(eq(heartbeatInterval));
    }

    private void initStationMockHeartbeat() {
        given(stationStoreMock.getHeartbeatInterval()).willReturn(DEFAULT_HEARTBEAT_INTERVAL);
    }

    static AttributePath.AttributePathBuilder attributePathBuilder() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(VARIABLE_NAME)));
    }

}
