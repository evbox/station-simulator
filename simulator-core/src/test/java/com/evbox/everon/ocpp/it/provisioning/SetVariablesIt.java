package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.mock.station.StationSimulatorSetUp;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.station.ExpectedResponses.responseWithId;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SetVariablesIt extends StationSimulatorSetUp {

    @Test
    void shouldReplyToSetVariablesRequest() {

        String id = UUID.randomUUID().toString();

        ocppMockServer.expectResponseFromStation(responseWithId(id));

        stationSimulatorRunner.run();

        SetVariablesRequest setVariablesRequest = createSetVariableRequest(
                "ReservationFeature",
                "ReserveConnectorZeroSupported",
                "true",
                SetVariableDatum.AttributeType.TARGET);

        Call call = new Call(id, ActionType.SET_VARIABLES, setVariablesRequest);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSetHeartbeatIntervalWithSetVariablesRequest() {

        int newHeartbeatInterval = 120;

        stationSimulatorRunner.run();

        SetVariablesRequest setVariablesRequest = createSetVariableRequest(
                OCPPCommCtrlrComponent.NAME,
                HeartbeatIntervalVariableAccessor.NAME,
                String.valueOf(newHeartbeatInterval),
                SetVariableDatum.AttributeType.ACTUAL);

        Call call = new Call(UUID.randomUUID().toString(), ActionType.SET_VARIABLES, setVariablesRequest);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            int heartbeatInterval = stationSimulatorRunner.getStation(STATION_ID).getState().getHeartbeatInterval();
            assertThat(heartbeatInterval).isEqualTo(newHeartbeatInterval);
            ocppMockServer.verify();
        });
    }

    SetVariablesRequest createSetVariableRequest(String component, String variable, String value, SetVariableDatum.AttributeType type) {
        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withComponent(new Component().withName(new CiString.CiString50(component)))
                .withVariable(new Variable().withName(new CiString.CiString50(variable)))
                .withAttributeType(type)
                .withAttributeValue(new CiString.CiString1000(value));

        return new SetVariablesRequest().withSetVariableData(singletonList(setVariableDatum));
    }
}
