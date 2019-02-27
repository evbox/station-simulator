package com.evbox.everon.ocpp.simulator.station.component.chargingstation;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationHardwareData;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.evbox.everon.ocpp.simulator.assertion.CiStringAssert.assertCiString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class ChargeProtocolVariableAccessorTest {

    @InjectMocks
    ChargeProtocolVariableAccessor variableAccessor;

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ChargingStationComponent.NAME, ChargeProtocolVariableAccessor.NAME, SetVariableDatum.AttributeType.ACTUAL, "some_value", SetVariableResult.AttributeStatus.REJECTED)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        return Stream.of(
                arguments(ChargingStationComponent.NAME, ChargeProtocolVariableAccessor.NAME, GetVariableDatum.AttributeType.ACTUAL, GetVariableResult.AttributeStatus.ACCEPTED, StationHardwareData.PROTOCOL_VERSION),
                arguments(ChargingStationComponent.NAME, ChargeProtocolVariableAccessor.NAME, GetVariableDatum.AttributeType.MAX_SET, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null)
        );
    }

    @ParameterizedTest
    @MethodSource("setVariableDatumProvider")
    void shouldValidateSetVariableDatum(String componentName, String variableName, SetVariableDatum.AttributeType attributeType, String value, SetVariableResult.AttributeStatus expectedAttributeStatus) {
        //when
        SetVariableResult result = variableAccessor.validate(
                new Component().withName(new CiString.CiString50(componentName)),
                new Variable().withName(new CiString.CiString50(variableName)),
                attributeType,
                new CiString.CiString1000(value)
        );

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(componentName);
        assertCiString(result.getVariable().getName()).isEqualTo(variableName);
        assertThat(result.getAttributeType()).isEqualTo(SetVariableResult.AttributeType.fromValue(attributeType.value()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
    }

    @ParameterizedTest
    @MethodSource("getVariableDatumProvider")
    void shouldGetVariableDatum(String componentName, String variableName, GetVariableDatum.AttributeType attributeType, GetVariableResult.AttributeStatus expectedAttributeStatus, String expectedValue) {
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
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }
}
