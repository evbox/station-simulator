package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.centralserver.Component;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableStatus;
import com.evbox.everon.ocpp.v201.message.centralserver.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_CONNECTORS;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.TRANSACTION_COMPONENT_NAME;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.TX_START_POINT_VARIABLE_NAME;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.UNPLUGGED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
public class TxStartPointVariableAccessorTest {

    private static final String TX_START_POINT_VALUE = "EVConnected,Authorized";

    private static final CiString.CiString1000 TX_START_POINT_ATTRIBUTE = new CiString.CiString1000(TX_START_POINT_VALUE);

    StationStore stationStore;
    Station station;

    TxStartPointVariableAccessor txStartPointVariableAccessor;

    @BeforeEach
    void setup() {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);
        station = new Station(stationConfiguration);
        stationStore = new StationStore(Clock.systemUTC(), DEFAULT_HEARTBEAT_INTERVAL, 100,
                Map.of(DEFAULT_EVSE_ID, new Evse(DEFAULT_EVSE_ID, List.of(new Connector(1, UNPLUGGED, AVAILABLE)))));
        txStartPointVariableAccessor = new TxStartPointVariableAccessor(station, stationStore);
    }
    @Test
    void shouldUpdateTxStartPoint() {
        VariableSetter variableSetter = txStartPointVariableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), TX_START_POINT_ATTRIBUTE);

        assertThat(txStartPointVariableAccessor.getStationStore().getTxStartPointValues())
                .hasSize(2)
                .containsExactlyInAnyOrder(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED);
    }

    @Test
    void expectValidationToPass() {
        SetVariableValidator setVariableValidator = txStartPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), TX_START_POINT_ATTRIBUTE);

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_START_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.ACCEPTED);
    }

    @Test
    void expectValidationToPassEmpty() {
        SetVariableValidator setVariableValidator = txStartPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), new CiString.CiString1000(""));

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_START_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.ACCEPTED);
    }

    @Test
    void expectValidationToFailOnInvalidValues() {
        SetVariableValidator setVariableValidator = txStartPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        CiString.CiString1000 invalidValues = new CiString.CiString1000("Authorized,Random");

        SetVariableResult result = setVariableValidator.validate(attributePath(), invalidValues);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.REJECTED);
    }

    private AttributePath attributePath() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(TRANSACTION_COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(TX_START_POINT_VARIABLE_NAME)))
                .attributeType(AttributeType.ACTUAL)
                .build();
    }
}
