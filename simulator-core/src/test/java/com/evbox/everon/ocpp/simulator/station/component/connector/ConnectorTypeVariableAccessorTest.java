package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.UNPLUGGED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

    ConnectorTypeVariableAccessor variableAccessor;

    @BeforeEach
    void setup() {
        StationStore stationStore = new StationStore(Clock.systemUTC(), 10, 100,
                Map.of(DEFAULT_EVSE_ID,  new Evse(DEFAULT_EVSE_ID, List.of(new Connector(1, UNPLUGGED, AVAILABLE)))));
        variableAccessor = new ConnectorTypeVariableAccessor(null, stationStore);
    }

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableStatus.REJECTED),
                arguments(MAX_SET_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(MIN_SET_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(TARGET_ATTRIBUTE, ConnectorTypeVariableAccessor.CONNECTOR_TYPE, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE)
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
                arguments(ACTUAL_ATTRIBUTE, GetVariableStatus.ACCEPTED, ConnectorTypeVariableAccessor.CONNECTOR_TYPE),
                arguments(MAX_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(MIN_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(TARGET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(actualWithUnknownEvse, GetVariableStatus.REJECTED, null),
                arguments(actualWithUnknownConnector, GetVariableStatus.REJECTED, null),
                arguments(actualWithUnknownEvseAndConnector, GetVariableStatus.REJECTED, null)
        );
    }

    @ParameterizedTest
    @MethodSource("setVariableDatumProvider")
    void shouldValidateSetVariableDatum(AttributePath attributePath, String value, SetVariableStatus expectedAttributeStatus) {
        //when
        SetVariableResult result = variableAccessor.validate(attributePath, new CiString.CiString1000(value));

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(Attribute.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
    }

    @ParameterizedTest
    @MethodSource("getVariableDatumProvider")
    void shouldGetVariableDatum(AttributePath attributePath, GetVariableStatus expectedAttributeStatus, String expectedValue) {
        //when
        GetVariableResult result = variableAccessor.get(attributePath);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(Attribute.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }

    static AttributePath.AttributePathBuilder attributePathBuilder(int evseId, int connectorId) {
        EVSE evse = new EVSE().withId(evseId).withConnectorId(connectorId);
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMPONENT_NAME))
                        .withEvse(evse)
                )
                .variable(new Variable().withName(new CiString.CiString50(VARIABLE_NAME)));
    }
}
