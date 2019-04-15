package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import com.evbox.everon.ocpp.v20.message.common.Evse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
import static com.google.common.base.Objects.equal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConnectorTypeVariableAccessorTest {

    private static final String COMPONENT_NAME = ConnectorComponent.NAME;
    private static final String VARIABLE_NAME = ConnectorTypeVariableAccessor.NAME;

    private static final int EVSE_ID = StationConstants.DEFAULT_EVSE_ID;
    private static final int UNKNOWN_EVSE_ID = 5;

    private static final int CONNECTOR_ID = StationConstants.DEFAULT_CONNECTOR_ID;
    private static final int UNKNOWN_CONNECTOR_ID = 2;

    private static final AttributePath ACTUAL_ATTRIBUTE = attributePathBuilder(EVSE_ID, CONNECTOR_ID)
            .attributeType(AttributeType.ACTUAL).build();

    private static final AttributePath MAX_SET_ATTRIBUTE = attributePathBuilder(EVSE_ID, CONNECTOR_ID)
            .attributeType(AttributeType.MAX_SET).build();

    private static final AttributePath MIN_SET_ATTRIBUTE = attributePathBuilder(EVSE_ID, CONNECTOR_ID)
            .attributeType(AttributeType.MIN_SET).build();

    private static final AttributePath TARGET_ATTRIBUTE = attributePathBuilder(EVSE_ID, CONNECTOR_ID)
            .attributeType(AttributeType.TARGET).build();

    @Mock(lenient = true)
    Station stationMock;
    @Mock(lenient = true)
    StationState stationStateMock;
    @Mock
    Connector connectorMock;

    @InjectMocks
    ConnectorTypeVariableAccessor variableAccessor;

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableResult.AttributeStatus.REJECTED),
                arguments(MAX_SET_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(MIN_SET_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(TARGET_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        AttributePath actualWithUnknownEvse = attributePathBuilder(UNKNOWN_EVSE_ID, CONNECTOR_ID)
                .attributeType(AttributeType.ACTUAL)
                .build();

        AttributePath actualWithUnknownConnector = attributePathBuilder(EVSE_ID, UNKNOWN_CONNECTOR_ID)
                .attributeType(AttributeType.ACTUAL)
                .build();

        AttributePath actualWithUnknownEvseAndConnector = attributePathBuilder(UNKNOWN_EVSE_ID, UNKNOWN_CONNECTOR_ID)
                .attributeType(AttributeType.ACTUAL)
                .build();

        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, GetVariableResult.AttributeStatus.ACCEPTED, ConnectorTypeVariableAccessor.CONNECTOR_TYPE),
                arguments(MAX_SET_ATTRIBUTE, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(MIN_SET_ATTRIBUTE, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(TARGET_ATTRIBUTE, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(actualWithUnknownEvse, GetVariableResult.AttributeStatus.REJECTED, null),
                arguments(actualWithUnknownConnector, GetVariableResult.AttributeStatus.REJECTED, null),
                arguments(actualWithUnknownEvseAndConnector, GetVariableResult.AttributeStatus.REJECTED, null)
        );
    }

    @ParameterizedTest
    @MethodSource("setVariableDatumProvider")
    void shouldValidateSetVariableDatum(AttributePath attributePath, String value, SetVariableResult.AttributeStatus expectedAttributeStatus) {
        //when
        SetVariableResult result = variableAccessor.validate(attributePath, new CiString.CiString1000(value));

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(SetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
    }

    @ParameterizedTest
    @MethodSource("getVariableDatumProvider")
    void shouldGetVariableDatum(AttributePath attributePath, GetVariableResult.AttributeStatus expectedAttributeStatus, String expectedValue) {
        //given
        initConnectorMock(EVSE_ID, CONNECTOR_ID);

        //when
        GetVariableResult result = variableAccessor.get(attributePath);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(GetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }

    private void initConnectorMock(Integer evseId, Integer connectorId) {
        given(stationMock.getStateView()).willReturn(stationStateMock);
        given(stationStateMock.tryFindConnector(anyInt(), anyInt()))
                .willAnswer(invocation -> equal(invocation.getArgument(0), evseId) && equal(invocation.getArgument(1), connectorId) ?
                        Optional.of(connectorMock) :
                        Optional.empty());
    }

    static AttributePath.AttributePathBuilder attributePathBuilder(int evseId, int connectorId) {
        Evse evse = new Evse().withId(evseId).withConnectorId(connectorId);
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMPONENT_NAME))
                        .withEvse(evse)
                )
                .variable(new Variable().withName(new CiString.CiString50(VARIABLE_NAME)));
    }
}