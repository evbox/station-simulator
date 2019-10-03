package com.evbox.everon.ocpp.simulator.station.component.evse;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
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
class EnabledVariableAccessorTest {

    private static final int EVSE_ID = StationConstants.DEFAULT_EVSE_ID;
    private static final int UNKNOWN_EVSE_ID = 5;

    private static final String COMPONENT_NAME = EVSEComponent.NAME;
    private static final String VARIABLE_NAME = EnabledVariableAccessor.NAME;

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
    StationPersistenceLayer stationPersistenceLayerMock;
    @SuppressWarnings("unused")
    @Mock
    Evse evseMock;

    @InjectMocks
    EnabledVariableAccessor variableAccessor;

    static Stream<Arguments> setVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, EnabledVariableAccessor.EVSE_ENABLED_STATUS, SetVariableResult.AttributeStatus.REJECTED),
                arguments(MAX_SET_ATTRIBUTE, EnabledVariableAccessor.EVSE_ENABLED_STATUS, SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(MIN_SET_ATTRIBUTE, EnabledVariableAccessor.EVSE_ENABLED_STATUS, SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(TARGET_ATTRIBUTE, EnabledVariableAccessor.EVSE_ENABLED_STATUS, SetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        AttributePath actualWithUnknownEvse = attributePathBuilder(UNKNOWN_EVSE_ID)
                .attributeType(AttributeType.ACTUAL)
                .build();

        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, GetVariableResult.AttributeStatus.ACCEPTED, EnabledVariableAccessor.EVSE_ENABLED_STATUS),
                arguments(MAX_SET_ATTRIBUTE, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(MIN_SET_ATTRIBUTE, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(TARGET_ATTRIBUTE, GetVariableResult.AttributeStatus.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(actualWithUnknownEvse, GetVariableResult.AttributeStatus.REJECTED, null)
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
        initEvseMock();

        //when
        GetVariableResult result = variableAccessor.get(attributePath);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(GetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }

    private void initEvseMock() {
        given(stationPersistenceLayerMock.hasEvse(eq(EVSE_ID))).willReturn(true);
        given(stationPersistenceLayerMock.hasEvse(eq(UNKNOWN_EVSE_ID))).willReturn(false);
    }

    static AttributePath.AttributePathBuilder attributePathBuilder(int evseId) {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMPONENT_NAME))
                        .withEvse(new com.evbox.everon.ocpp.v20.message.common.Evse().withId(evseId))
                )
                .variable(new Variable().withName(new CiString.CiString50(VARIABLE_NAME)));
    }

}
