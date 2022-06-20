package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.Component;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableStatus;
import com.evbox.everon.ocpp.v201.message.centralserver.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.TRANSACTION_COMPONENT_NAME;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.TX_STOP_POINT_VARIABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class TxStopPointVariableAccessorTest {

    private static final String TX_STOP_POINT_VALUE = "EVConnected,Authorized";

    private static final CiString.CiString1000 TX_STOP_POINT_ATTRIBUTE = new CiString.CiString1000(TX_STOP_POINT_VALUE);

    Station station;

    TxStopPointVariableAccessor txStoptPointVariableAccessor;

    @BeforeEach
    void setUp() {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);
        station = new Station(stationConfiguration);
        txStoptPointVariableAccessor =new TxStopPointVariableAccessor(station, null);
    }
    @Test
    void shouldUpdateTxStopPoint() {
        VariableSetter variableSetter = txStoptPointVariableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), TX_STOP_POINT_ATTRIBUTE);

        assertThat(station.getState().getTxStartPointValues())
                .hasSize(2)
                .containsExactlyInAnyOrder(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED);
    }

    @Test
    void expectValidationToPass() {
        SetVariableValidator setVariableValidator = txStoptPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), TX_STOP_POINT_ATTRIBUTE);

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_STOP_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.ACCEPTED);
    }

    @Test
    void expectValidationToPassEmpty() {
        SetVariableValidator setVariableValidator = txStoptPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), new CiString.CiString1000(""));

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_STOP_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.ACCEPTED);
    }

    @Test
    void expectValidationToFailOnInvalidValues() {
        SetVariableValidator setVariableValidator = txStoptPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        CiString.CiString1000 invalidValues = new CiString.CiString1000("Authorized,Random");

        SetVariableResult result = setVariableValidator.validate(attributePath(), invalidValues);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.REJECTED);
    }

    private AttributePath attributePath() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(TRANSACTION_COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(TX_STOP_POINT_VARIABLE_NAME)))
                .attributeType(AttributeType.ACTUAL)
                .build();
    }
}
