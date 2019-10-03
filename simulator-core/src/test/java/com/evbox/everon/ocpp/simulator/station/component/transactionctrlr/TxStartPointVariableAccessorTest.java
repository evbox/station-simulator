package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.mock.constants.VariableConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TxStartPointVariableAccessorTest {

    private static final String TX_START_POINT_VALUE = "EVConnected,Authorized";

    private static final CiString.CiString1000 TX_START_POINT_ATTRIBUTE = new CiString.CiString1000(TX_START_POINT_VALUE);

    @Mock
    Station stationMock;

    @InjectMocks
    TxStartPointVariableAccessor txStartPointVariableAccessor;

    @Test
    void shouldUpdateTxStartPoint() {
        VariableSetter variableSetter = txStartPointVariableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), TX_START_POINT_ATTRIBUTE);

        verify(stationMock).updateTxStartPointValues(argThat(arg -> arg.contains(TxStartStopPointVariableValues.AUTHORIZED) && arg.contains(TxStartStopPointVariableValues.EV_CONNECTED)));
    }

    @Test
    void expectValidationToPass() {
        SetVariableValidator setVariableValidator = txStartPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), TX_START_POINT_ATTRIBUTE);

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_START_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.ACCEPTED);
    }

    @Test
    void expectValidationToPassEmpty() {
        SetVariableValidator setVariableValidator = txStartPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), new CiString.CiString1000(""));

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_START_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.ACCEPTED);
    }

    @Test
    void expectValidationToFailOnInvalidValues() {
        SetVariableValidator setVariableValidator = txStartPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        CiString.CiString1000 invalidValues = new CiString.CiString1000("Authorized,Random");

        SetVariableResult result = setVariableValidator.validate(attributePath(), invalidValues);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.INVALID_VALUE);
    }

    private AttributePath attributePath() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(TRANSACTION_COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(TX_START_POINT_VARIABLE_NAME)))
                .attributeType(AttributeType.ACTUAL)
                .build();
    }
}