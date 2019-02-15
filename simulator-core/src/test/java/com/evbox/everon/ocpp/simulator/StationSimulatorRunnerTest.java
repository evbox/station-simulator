package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.simulator.support.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.simulator.support.SimulatorConfigCreator.createStationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

public class StationSimulatorRunnerTest {

    private static final String STATION_ID_1 = "EVB-P17390866";
    private static final String STATION_ID_2 = "EVB-P18090564";
    private static final int STATION_1_EVSE_COUNT = 1;
    private static final int STATION_2_EVSE_COUNT = 1;
    private static final int STATION_1_CONNECTORS_PER_EVSE = 1;
    private static final int STATION_2_CONNECTORS_PER_EVSE = 2;

    @Test
    void shouldRespectRunConfiguration() {
        //given
        String url = "ws://localhost:8083";

        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(
                createStationConfiguration(STATION_ID_1, STATION_1_EVSE_COUNT, STATION_1_CONNECTORS_PER_EVSE),
                createStationConfiguration(STATION_ID_2, STATION_2_EVSE_COUNT, STATION_2_CONNECTORS_PER_EVSE)
        );

        StationSimulatorRunner simulator = new StationSimulatorRunner(url, simulatorConfiguration);

        //when
        simulator.run();

        //then
        assertThat(simulator.getStation(STATION_ID_1)).isNotNull();
        assertThat(simulator.getStation(STATION_ID_2)).isNotNull();
        assertThat(simulator.getStation(STATION_ID_1).getConfiguration().getEvse().getConnectors()).isEqualTo(STATION_1_CONNECTORS_PER_EVSE);
        assertThat(simulator.getStation(STATION_ID_1).getConfiguration().getEvse().getCount()).isEqualTo(STATION_1_EVSE_COUNT);
        assertThat(simulator.getStation(STATION_ID_2).getConfiguration().getEvse().getConnectors()).isEqualTo(STATION_2_CONNECTORS_PER_EVSE);
        assertThat(simulator.getStation(STATION_ID_2).getConfiguration().getEvse().getCount()).isEqualTo(STATION_2_EVSE_COUNT);
    }
}