package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SetVariablesIt extends StationSimulatorSetUp {

    @Test
    void shouldReplyToSetVariablesRequest() {

        int expectedNumberOfVariables = 1;

        stationSimulatorRunner.run();

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                "ReservationFeature",
                "ReserveConnectorZeroSupported",
                "true",
                SetVariableDatum.AttributeType.TARGET);

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

        stationSimulatorRunner.run();

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                OCPPCommCtrlrComponent.NAME,
                HeartbeatIntervalVariableAccessor.NAME,
                String.valueOf(newHeartbeatInterval),
                SetVariableDatum.AttributeType.ACTUAL);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_VARIABLES, setVariablesRequest);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            int heartbeatInterval = stationSimulatorRunner.getStation(STATION_ID).getStateView().getHeartbeatInterval();
            assertThat(heartbeatInterval).isEqualTo(newHeartbeatInterval);
            ocppMockServer.verify();
        });
    }

    SetVariablesRequest createSetVariablesRequest(String component, String variable, String value, SetVariableDatum.AttributeType type) {
        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withComponent(new Component().withName(new CiString.CiString50(component)))
                .withVariable(new Variable().withName(new CiString.CiString50(variable)))
                .withAttributeType(type)
                .withAttributeValue(new CiString.CiString1000(value));

        return new SetVariablesRequest().withSetVariableData(singletonList(setVariableDatum));
    }
}
