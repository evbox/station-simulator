package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.EVConnectionTimeOutVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxCtrlrComponent;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariablesResponse;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.factory.SetVariablesCreator.createSetVariablesRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class SetVariablesIt extends StationSimulatorSetUp {

    @Test
    void shouldReplyToSetVariablesRequest() {

        int expectedNumberOfVariables = 1;

        stationSimulatorRunner.run();

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                "ReservationFeature",
                "ReserveConnectorZeroSupported",
                "true",
                Attribute.TARGET);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_VARIABLES, setVariablesRequest);

        ocppMockServer.waitUntilConnected();

        SetVariablesResponse response =
                ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), SetVariablesResponse.class);

        await().untilAsserted(() -> {
            assertThat(response.getSetVariableResult().size()).isEqualTo(expectedNumberOfVariables);
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSetHeartbeatIntervalWithSetVariablesRequest() {

        int newHeartbeatInterval = 120;

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                OCPPCommCtrlrComponent.NAME,
                HeartbeatIntervalVariableAccessor.NAME,
                String.valueOf(newHeartbeatInterval),
                Attribute.ACTUAL);

        shouldSetHeartbeatIntervalWithSetVariablesRequestImpl(setVariablesRequest, newHeartbeatInterval);
    }

    @Test
    void shouldSetHeartbeatIntervalWithSetVariablesRequestUpperCase() {

        int newHeartbeatInterval = 120;

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                OCPPCommCtrlrComponent.NAME.toUpperCase(),
                HeartbeatIntervalVariableAccessor.NAME.toUpperCase(),
                String.valueOf(newHeartbeatInterval),
                Attribute.ACTUAL);

        shouldSetHeartbeatIntervalWithSetVariablesRequestImpl(setVariablesRequest, newHeartbeatInterval);
    }

    @Test
    void shouldSetEVConnectionTimeOutWithSetVariablesRequest() {

        int evConnectionTimeout = 10;
        stationSimulatorRunner.run();

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                TxCtrlrComponent.NAME,
                EVConnectionTimeOutVariableAccessor.NAME,
                String.valueOf(evConnectionTimeout),
                Attribute.ACTUAL);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_VARIABLES, setVariablesRequest);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            int connectionTimeout = stationSimulatorRunner.getStation(STATION_ID).getStateView().getEvConnectionTimeOut();
            assertThat(connectionTimeout).isEqualTo(evConnectionTimeout);
            ocppMockServer.verify();
        });
    }

    private void shouldSetHeartbeatIntervalWithSetVariablesRequestImpl(SetVariablesRequest setVariablesRequest,
                                                                       int newHeartbeatInterval) {

        stationSimulatorRunner.run();

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_VARIABLES, setVariablesRequest);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            int heartbeatInterval = stationSimulatorRunner.getStation(STATION_ID).getStateView().getHeartbeatInterval();
            assertThat(heartbeatInterval).isEqualTo(newHeartbeatInterval);
            ocppMockServer.verify();
        });
    }
}
