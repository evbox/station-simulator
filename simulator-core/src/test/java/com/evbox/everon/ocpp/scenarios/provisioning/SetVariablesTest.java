package com.evbox.everon.ocpp.scenarios.provisioning;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.testutil.StationSimulatorSetUp;
import com.evbox.everon.ocpp.testutil.expect.ExpectedCount;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.*;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.bootNotificationResponseMock;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.emptyResponse;
import static com.evbox.everon.ocpp.testutil.station.ExpectedResponses.anyResponse;
import static com.evbox.everon.ocpp.testutil.station.ExpectedResponses.responseWithId;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SetVariablesTest extends StationSimulatorSetUp {

    @Test
    void shouldReplyToSetVariablesRequest() {
        //given
        String id = UUID.randomUUID().toString();

        ocppMockServer
                .when(bootNotificationRequest())
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(AVAILABLE))
                .thenReturn(emptyResponse());

        ocppMockServer
                .when(heartbeatRequest())
                .thenReturn(emptyResponse());

        ocppMockServer
                .expectResponseFromStation(responseWithId(id));

        //when
        stationSimulatorRunner.run();

        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withVariable(new Variable().withName(new CiString.CiString50("ReserveConnectorZeroSupported")))
                .withComponent(new Component().withName(new CiString.CiString50("ReservationFeature")))
                .withAttributeValue(new CiString.CiString1000("true")).withAttributeType(SetVariableDatum.AttributeType.TARGET);

        SetVariablesRequest request = new SetVariablesRequest().withSetVariableData(singletonList(setVariableDatum));

        Call call = new Call(id, ActionType.SET_VARIABLES, request);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        //then
        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSetHeartbeatIntervalWithSetVariablesRequest() {
        //given
        int newHeartbeatInterval = 120;

        ocppMockServer
                .when(bootNotificationRequest())
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(AVAILABLE))
                .thenReturn(emptyResponse());

        ocppMockServer
                .when(heartbeatRequest())
                .thenReturn(emptyResponse());

        ocppMockServer
                .expectResponseFromStation(anyResponse(), ExpectedCount.atLeastOnce());

        //when
        stationSimulatorRunner.run();

        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withComponent(new Component().withName(new CiString.CiString50(OCPPCommCtrlrComponent.NAME)))
                .withVariable(new Variable().withName(new CiString.CiString50(HeartbeatIntervalVariableAccessor.NAME)))
                .withAttributeType(SetVariableDatum.AttributeType.ACTUAL)
                .withAttributeValue(new CiString.CiString1000(String.valueOf(newHeartbeatInterval)));

        Call call = new Call(UUID.randomUUID().toString(), ActionType.SET_VARIABLES, new SetVariablesRequest().withSetVariableData(singletonList(setVariableDatum)));

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        //then
        await().untilAsserted(() -> {
            int heartbeatInterval = stationSimulatorRunner.getStation(STATION_ID).getState().getHeartbeatInterval();
            assertThat(heartbeatInterval).isEqualTo(newHeartbeatInterval);
            ocppMockServer.verify();
        });
    }
}
