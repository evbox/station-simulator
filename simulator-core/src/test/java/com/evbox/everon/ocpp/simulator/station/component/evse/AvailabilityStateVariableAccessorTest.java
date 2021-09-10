package com.evbox.everon.ocpp.simulator.station.component.evse;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AvailabilityStateVariableAccessorTest {

    private static final int EVSE_ID = StationConstants.DEFAULT_EVSE_ID;
    private static final int UNKNOWN_EVSE_ID = 5;

    private static final String COMPONENT_NAME = EVSEComponent.NAME;
    private static final String VARIABLE_NAME = AvailabilityStateVariableAccessor.NAME;

    private static final AttributePath ACTUAL_ATTRIBUTE = attributePathBuilder(EVSE_ID)
            .attributeType(AttributeType.ACTUAL).build();

    private static final AttributePath MAX_SET_ATTRIBUTE = attributePathBuilder(EVSE_ID)
            .attributeType(AttributeType.MAX_SET).build();

    private static final AttributePath MIN_SET_ATTRIBUTE = attributePathBuilder(EVSE_ID)
            .attributeType(AttributeType.MIN_SET).build();

    private static final AttributePath TARGET_ATTRIBUTE = attributePathBuilder(EVSE_ID)
            .attributeType(AttributeType.TARGET).build();

    @SuppressWarnings("unused")
    @Mock(lenient = true)
    Station stationMock;
    @Mock(lenient = true)
    StationStore stationStoreMock;
    @SuppressWarnings("unused")
    @Mock
    Evse evseMock;

    @InjectMocks
    AvailabilityStateVariableAccessor variableAccessor;

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, AvailabilityStateVariableAccessor.EVSE_AVAILABILITY, SetVariableStatus.REJECTED),
                arguments(MAX_SET_ATTRIBUTE, AvailabilityStateVariableAccessor.EVSE_AVAILABILITY, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(MIN_SET_ATTRIBUTE, AvailabilityStateVariableAccessor.EVSE_AVAILABILITY, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(TARGET_ATTRIBUTE, AvailabilityStateVariableAccessor.EVSE_AVAILABILITY, SetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        AttributePath actualWithUnknownEvse = attributePathBuilder(UNKNOWN_EVSE_ID)
                .attributeType(AttributeType.ACTUAL)
                .build();

        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, GetVariableStatus.ACCEPTED, AvailabilityStateVariableAccessor.EVSE_AVAILABILITY),
                arguments(MAX_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(MIN_SET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(TARGET_ATTRIBUTE, GetVariableStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(actualWithUnknownEvse, GetVariableStatus.REJECTED, null)
        );
    }

    @ParameterizedTest
    @MethodSource("setVariableDatumProvider")
    void shouldValidateSetVariableDatum(AttributePath attributePath, String value, SetVariableStatus expectedAttributeStatus) {
        //when
        SetVariableResult result = variableAccessor.validate(attributePath, new CiString.CiString1000(value)
        );

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
        initEvseMock();

        //when
        GetVariableResult result = variableAccessor.get(attributePath);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(Attribute.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }

    private void initEvseMock() {
        given(stationStoreMock.hasEvse(eq(EVSE_ID))).willReturn(true);
        given(stationStoreMock.hasEvse(eq(UNKNOWN_EVSE_ID))).willReturn(false);
    }

    static AttributePath.AttributePathBuilder attributePathBuilder(int evseId) {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMPONENT_NAME))
                        .withEvse(new EVSE().withId(evseId))
                )
                .variable(new Variable().withName(new CiString.CiString50(VARIABLE_NAME)));
    }
}
