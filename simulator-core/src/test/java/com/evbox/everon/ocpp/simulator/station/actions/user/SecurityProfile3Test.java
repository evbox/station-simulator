package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class SecurityProfile3Test {

    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    StateManager stateManagerMock;

    Station station;
    SecurityProfile3 securityProfile3;

    @BeforeEach
    void setUp() {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);

        this.station = new Station(stationConfiguration);
        this.securityProfile3 = new SecurityProfile3("url");
        this.stateManagerMock = new StateManager(station, stationStoreMock, stationMessageSenderMock);
    }

    @Test
    void shouldThrowExceptionOnIllegalState() {
        assertThrows(IllegalArgumentException.class, () -> securityProfile3.perform(stateManagerMock));
    }
}
