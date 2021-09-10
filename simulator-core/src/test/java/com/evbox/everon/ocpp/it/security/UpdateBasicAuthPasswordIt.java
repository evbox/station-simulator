package com.evbox.everon.ocpp.it.security;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariablesRequest;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.BASIC_AUTH_PASSWORD_VARIABLE_NAME;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.SECURITY_COMPONENT_NAME;
import static com.evbox.everon.ocpp.mock.factory.SetVariablesCreator.createSetVariablesRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class UpdateBasicAuthPasswordIt extends StationSimulatorSetUp {

    @Test
    void shouldSetBasicAuthPassword() {

        String expectedPassword = "aabbcc";

        stationSimulatorRunner.run();

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                SECURITY_COMPONENT_NAME,
                BASIC_AUTH_PASSWORD_VARIABLE_NAME,
                expectedPassword,
                Attribute.ACTUAL);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_VARIABLES, setVariablesRequest);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            SimulatorConfiguration.StationConfiguration configuration = stationSimulatorRunner.getStation(STATION_ID).getConfiguration();

            assertThat(configuration.getComponentsConfiguration().getSecurityCtrlr().getBasicAuthPassword()).isEqualTo(expectedPassword);

            ocppMockServer.verify();
        });
    }
}
