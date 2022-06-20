package com.evbox.everon.ocpp.simulator.station.component.authctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.HashMap;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class AuthorizeStateVariableAccessorTest {

    private static final String COMPONENT_NAME = AuthCtrlrComponent.NAME;
    private static final String VARIABLE_NAME = AuthorizeStateVariableAccessor.NAME;

    private static final AttributePath ACTUAL_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.ACTUAL).build();
    private static final AttributePath MAX_SET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.MAX_SET).build();
    private static final AttributePath MIN_SET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.MIN_SET).build();
    private static final AttributePath TARGET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.TARGET).build();

    Station station;
    StationStore stationStore;

    AuthorizeStateVariableAccessor authorizeAccessor;

    @BeforeEach
    void setUp() {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);
        station = new Station(stationConfiguration);
        stationStore = new StationStore(Clock.systemUTC(), 10, 100, new HashMap<>());
        authorizeAccessor = new AuthorizeStateVariableAccessor(station, stationStore);

    }
    static AttributePath.AttributePathBuilder attributePathBuilder() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(VARIABLE_NAME)));
    }

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, "true", SetVariableStatus.ACCEPTED),
                arguments(ACTUAL_ATTRIBUTE, "false", SetVariableStatus.ACCEPTED),
                arguments(ACTUAL_ATTRIBUTE, "yes", SetVariableStatus.REJECTED),
                arguments(MAX_SET_ATTRIBUTE, "true", SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(MIN_SET_ATTRIBUTE, "true", SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(TARGET_ATTRIBUTE, "true", SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, GetVariableStatus.ACCEPTED, String.valueOf(true)),
                arguments(MAX_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(MIN_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(TARGET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null)
        );
    }

    @ParameterizedTest
    @MethodSource("setVariableDatumProvider")
    void shouldValidateSetVariableDatum(AttributePath attributePath, String authEnabled, SetVariableStatus expectedAttributeStatus) {
        //when
        SetVariableResult result = authorizeAccessor.validate(attributePath, new CiString.CiString1000(authEnabled));

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
        initStationMockAuthEnabled();

        //when
        GetVariableResult result = authorizeAccessor.get(attributePath);

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
        Component component = new Component().withName(new CiString.CiString50(AuthCtrlrComponent.NAME));
        Variable variable = new Variable().withName(new CiString.CiString50(AuthorizeStateVariableAccessor.NAME));
        Attribute attributeType = Attribute.ACTUAL;

        //when
        authorizeAccessor.setActualValue(new AttributePath(component, variable, attributeType), new CiString.CiString1000(String.valueOf(false)));

        //then
        assertFalse(station.getState().isAuthEnabled());
    }

    private void initStationMockAuthEnabled() {
        stationStore.setAuthorizeState(true);
    }

}