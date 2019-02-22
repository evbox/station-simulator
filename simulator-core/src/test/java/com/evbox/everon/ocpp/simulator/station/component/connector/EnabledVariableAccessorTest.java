package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.simulator.assertion.CiStringAssert.assertCiString;
import static com.google.common.base.Objects.equal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EnabledVariableAccessorTest {

    private static final int EVSE_ID = 1;
    private static final int UNKNOWN_EVSE_ID = 5;

    private static final int CONNECTOR_ID = 1;
    private static final int UNKNOWN_CONNECTOR_ID = 2;

    @Mock(lenient = true)
    Station stationMock;
    @Mock(lenient = true)
    StationState stationStateMock;
    @Mock
    Connector connectorMock;

    @InjectMocks
    EnabledVariableAccessor variableAccessor;

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ConnectorComponent.NAME, EnabledVariableAccessor.NAME, SetVariableDatum.AttributeType.ACTUAL, EnabledVariableAccessor.CONNECTOR_STATUS, SetVariableResult.AttributeStatus.REJECTED)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        return Stream.of(
                arguments(ConnectorComponent.NAME, EnabledVariableAccessor.NAME, EVSE_ID, CONNECTOR_ID, GetVariableDatum.AttributeType.ACTUAL, GetVariableResult.AttributeStatus.ACCEPTED, EnabledVariableAccessor.CONNECTOR_STATUS),
                arguments(ConnectorComponent.NAME, EnabledVariableAccessor.NAME, EVSE_ID, CONNECTOR_ID, GetVariableDatum.AttributeType.MAX_SET, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(ConnectorComponent.NAME, EnabledVariableAccessor.NAME, UNKNOWN_EVSE_ID, CONNECTOR_ID, GetVariableDatum.AttributeType.ACTUAL, GetVariableResult.AttributeStatus.REJECTED, null),
                arguments(ConnectorComponent.NAME, EnabledVariableAccessor.NAME, EVSE_ID, UNKNOWN_CONNECTOR_ID, GetVariableDatum.AttributeType.ACTUAL, GetVariableResult.AttributeStatus.REJECTED, null),
                arguments(ConnectorComponent.NAME, EnabledVariableAccessor.NAME, UNKNOWN_EVSE_ID, UNKNOWN_CONNECTOR_ID, GetVariableDatum.AttributeType.ACTUAL, GetVariableResult.AttributeStatus.REJECTED, null)
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
    void shouldGetVariableDatum(String componentName, String variableName, Integer evseId, Integer connectorId, GetVariableDatum.AttributeType attributeType, GetVariableResult.AttributeStatus expectedAttributeStatus, String expectedValue) {
        //given
        initConnectorMock(EVSE_ID, CONNECTOR_ID);

        //when
        Evse evse = new Evse()
                .withId(evseId)
                .withConnectorId(connectorId);

        GetVariableResult result = variableAccessor.get(
                new Component().withName(new CiString.CiString50(componentName))
                        .withEvse(evse),
                new Variable().withName(new CiString.CiString50(variableName)),
                attributeType);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(componentName);
        assertCiString(result.getVariable().getName()).isEqualTo(variableName);
        assertThat(result.getAttributeType()).isEqualTo(GetVariableResult.AttributeType.fromValue(attributeType.value()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }

    private void initConnectorMock(Integer evseId, Integer connectorId) {
        given(stationMock.getState()).willReturn(stationStateMock);
        given(stationStateMock.tryFindConnector(anyInt(), anyInt()))
                .willAnswer(invocation -> equal(invocation.getArgument(0), evseId) && equal(invocation.getArgument(1), connectorId) ?
                        Optional.of(connectorMock) :
                        Optional.empty());
    }
}