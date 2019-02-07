package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.evbox.everon.ocpp.simulator.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.simulator.SimulatorConfigCreator.createStationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StationSimulatorTest {

    private static final String STATION_ID_1 = "EVB-P17390866";
    private static final String STATION_ID_2 = "EVB-P18090564";
    private static final int STATION_1_EVSE_COUNT = 1;
    private static final int STATION_2_EVSE_COUNT = 1;
    private static final int STATION_1_CONNECTORS_PER_EVSE = 1;
    private static final int STATION_2_CONNECTORS_PER_EVSE = 2;

    @Test
    public void shouldRespectRunConfiguration() {
        //given
        String url = "ws://localhost:8083";

        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(
                createStationConfiguration(STATION_ID_1, STATION_1_EVSE_COUNT, STATION_1_CONNECTORS_PER_EVSE),
                createStationConfiguration(STATION_ID_2, STATION_2_EVSE_COUNT, STATION_2_CONNECTORS_PER_EVSE)
        );

        StationSimulator simulator = new StationSimulator(url, simulatorConfiguration);

        //when
        simulator.start();

        //then
        assertThat(simulator.getStation(STATION_ID_1)).isNotNull();
        assertThat(simulator.getStation(STATION_ID_2)).isNotNull();
        assertThat(simulator.getStation(STATION_ID_1).getConfiguration().getConnectorsPerEvseCount()).isEqualTo(STATION_1_CONNECTORS_PER_EVSE);
        assertThat(simulator.getStation(STATION_ID_1).getConfiguration().getEvseCount()).isEqualTo(STATION_1_EVSE_COUNT);
        assertThat(simulator.getStation(STATION_ID_2).getConfiguration().getConnectorsPerEvseCount()).isEqualTo(STATION_2_CONNECTORS_PER_EVSE);
        assertThat(simulator.getStation(STATION_ID_2).getConfiguration().getEvseCount()).isEqualTo(STATION_2_EVSE_COUNT);
    }
}