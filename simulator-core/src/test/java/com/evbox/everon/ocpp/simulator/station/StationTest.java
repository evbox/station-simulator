package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static com.evbox.everon.ocpp.mock.ReflectionUtils.injectMock;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StationTest {

    @Mock
    WebSocketClient webSocketClientMock;

    private Station station;

    @BeforeEach
    void setUp() {

        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);

        station = new Station(stationConfiguration);

        injectMock(station, "webSocketClient", webSocketClientMock);
    }

    @Test
    void verifyStatOutputFields() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String statOutput = station.getStateView().toString();
        Set<String> expectedFields = new HashSet<>(Arrays.asList("heartbeatInterval", "evses", "currentTime", "evConnectionTimeOut", "displayMessages"));

        JsonNode node = objectMapper.readTree(statOutput);
        Iterator<String> fieldsIterator = node.fieldNames();
        int fieldsCount = 0;
        while (fieldsIterator.hasNext()) {
            assertThat(expectedFields.contains(fieldsIterator.next())).isTrue();
            fieldsCount++;
        }
        assertThat(fieldsCount).isEqualTo(expectedFields.size());
    }

    @Test
    void testStationConfigurationParameters() {

        final int evConnectionTimeOut = 200;
        final String basicPassword = "Password123";

        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);
        stationConfiguration.getComponentsConfiguration().getTxCtrlr().setEvConnectionTimeOutSec(evConnectionTimeOut);
        stationConfiguration.getComponentsConfiguration().getSecurityCtrlr().setBasicAuthPassword(basicPassword);

        station = new Station(stationConfiguration);

        assertThat(station.getStateView().getEvConnectionTimeOut()).isEqualTo(evConnectionTimeOut);
        assertThat(station.getStateView().getEvses().size()).isEqualTo(DEFAULT_EVSE_COUNT);
        station.getStateView().getEvses().forEach(e -> assertThat(e.getConnectors().size()).isEqualTo(DEFAULT_EVSE_CONNECTORS));
        assertThat(station.getId()).isEqualTo(STATION_ID);
        assertThat(station.getConfiguration().getComponentsConfiguration().getSecurityCtrlr().getBasicAuthPassword()).isEqualTo(basicPassword);
    }

}
