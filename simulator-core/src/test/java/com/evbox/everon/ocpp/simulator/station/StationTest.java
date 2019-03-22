package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.support.ReflectionUtils.injectMock;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
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
    void verifyConnectionUrl() {

        station.connectToServer(SERVER_BASE_URL);

        ArgumentCaptor<String> urlCapture = ArgumentCaptor.forClass(String.class);

        verify(webSocketClientMock).connect(urlCapture.capture());

        String expectedUrl = SERVER_BASE_URL + "/" + STATION_ID;

        assertEquals(expectedUrl, urlCapture.getValue());

    }

}