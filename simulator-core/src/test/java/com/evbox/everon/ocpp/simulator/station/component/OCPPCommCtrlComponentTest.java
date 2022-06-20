package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.UNPLUGGED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

class OCPPCommCtrlComponentTest {

    HeartbeatIntervalVariableAccessor heartbeatIntervalVariableAccessor;
    Station station;
    StationStore stationStore;

    OCPPCommCtrlrComponent ocppCommCtrlrComponent;

    @BeforeEach
    void setUp() throws Exception {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);
        station = new Station(stationConfiguration);
        stationStore = new StationStore(Clock.systemUTC(), DEFAULT_HEARTBEAT_INTERVAL, 100,
                Map.of(DEFAULT_EVSE_ID, new Evse(DEFAULT_EVSE_ID, List.of(new Connector(1, UNPLUGGED, AVAILABLE)))));
        heartbeatIntervalVariableAccessor = new HeartbeatIntervalVariableAccessor(station, stationStore);
        ocppCommCtrlrComponent = new OCPPCommCtrlrComponent(station, stationStore);
        Map<CiString.CiString50, VariableAccessor> variableAccessors
                = ImmutableMap.of(new CiString.CiString50(ocppCommCtrlrComponent.getComponentName() + "-" + HeartbeatIntervalVariableAccessor.NAME), heartbeatIntervalVariableAccessor);

        FieldUtils.writeField(ocppCommCtrlrComponent, "variableAccessors", variableAccessors, true);
    }

    @Test
    void shouldReturnGetVariableResult() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        Attribute attributeType = Attribute.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        GetVariableData getVariableDatum = new GetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType);

        //when
        GetVariableResult result = ocppCommCtrlrComponent.getVariable(getVariableDatum);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(componentName);
        assertCiString(result.getVariable().getName()).isEqualTo(variableName);
        assertCiString(result.getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(result.getAttributeType()).isEqualTo(Attribute.fromValue(attributeType.value()));
    }

    @Test
    void shouldReturnSetVariableResult() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        AttributeType attributeType = AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableData setVariableDatum = new SetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(Attribute.fromValue(attributeType.getName()))
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        ocppCommCtrlrComponent.setVariable(setVariableDatum);

        //then
        CiString.CiString2500 attributeActualValue = ocppCommCtrlrComponent.getVariableAccessorByName(variableName)
                .get(new AttributePath(component, variable, Attribute.fromValue(attributeType.getName()))).getAttributeValue();
        assertCiString(attributeActualValue).isEqualTo(String.valueOf(variableValue));
    }

    @Test
    void shouldValidateSetVariableDatum() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = HeartbeatIntervalVariableAccessor.NAME;
        AttributeType attributeType = AttributeType.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableData setVariableDatum = new SetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(Attribute.fromValue(attributeType.getName()))
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        SetVariableValidationResult validationResult = ocppCommCtrlrComponent.validate(setVariableDatum);

        //then
        assertCiString(validationResult.getSetVariableData().getComponent().getName()).isEqualTo(componentName);
        assertCiString(validationResult.getSetVariableData().getVariable().getName()).isEqualTo(variableName);
        assertCiString(validationResult.getSetVariableData().getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(validationResult.getSetVariableData().getAttributeType()).isEqualTo(Attribute.ACTUAL);
        assertThat(validationResult.getResult().getAttributeStatus()).isEqualTo(SetVariableStatus.ACCEPTED);
    }

    @Test
    void shouldValidateIfVariableExists() {
        //given
        String componentName = OCPPCommCtrlrComponent.NAME;
        String variableName = "UnknownVariableName";
        Attribute attributeType = Attribute.ACTUAL;
        int variableValue = StationConstants.DEFAULT_HEARTBEAT_INTERVAL;

        Component component = new Component().withName(new CiString.CiString50(componentName));
        Variable variable = new Variable().withName(new CiString.CiString50(variableName));

        SetVariableData setVariableDatum = new SetVariableData()
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(attributeType)
                .withAttributeValue(new CiString.CiString1000(String.valueOf(variableValue)));

        //when
        SetVariableValidationResult validationResult = ocppCommCtrlrComponent.validate(setVariableDatum);

        //then
        assertCiString(validationResult.getSetVariableData().getComponent().getName()).isEqualTo(componentName);
        assertCiString(validationResult.getSetVariableData().getVariable().getName()).isEqualTo(variableName);
        assertCiString(validationResult.getSetVariableData().getAttributeValue()).isEqualTo(String.valueOf(variableValue));
        assertThat(validationResult.getSetVariableData().getAttributeType()).isEqualTo(Attribute.ACTUAL);
        assertThat(validationResult.getResult().getAttributeStatus()).isEqualTo(SetVariableStatus.UNKNOWN_VARIABLE);
    }

}
