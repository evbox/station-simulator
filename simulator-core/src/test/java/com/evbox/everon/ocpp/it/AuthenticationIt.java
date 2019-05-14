package com.evbox.everon.ocpp.it;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createStationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthenticationIt extends StationSimulatorSetUp {

    void setUp(String username, String password) {
        SimulatorConfiguration.StationConfiguration stationConfiguration = createStationConfiguration(
                STATION_ID,
                EVSE_COUNT_TWO,
                EVSE_CONNECTORS_TWO,
                username,
                password
        );
        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(stationConfiguration);
        stationSimulatorRunner = new StationSimulatorRunner(OCPP_SERVER_URL, simulatorConfiguration);
    }

    @Test
    void expectToFailAuth() {

        setUp("invalid-username", "invalid-password");

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilAuthorized();

        Map<String, String> receivedCredentials = ocppMockServer.getReceivedCredentials();

        assertAll(
                () -> assertThat(receivedCredentials).hasSize(1),
                () -> assertNull(receivedCredentials.get(BASIC_AUTH_USERNAME))
        );
    }

    @Test
    void expectSuccessfulAuth() {

        setUp(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilAuthorized();

        Map<String, String> receivedCredentials = ocppMockServer.getReceivedCredentials();

        assertAll(
                () -> assertThat(receivedCredentials).hasSize(1),
                () -> assertThat(receivedCredentials.get(BASIC_AUTH_USERNAME)).isEqualTo(BASIC_AUTH_PASSWORD)
        );
    }
}
