package com.evbox.everon.ocpp.functional.provisioning;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.testutil.station.StationSimulatorSetUp;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutil.station.ExpectedResponses.responseWithId;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetVariablesTest extends StationSimulatorSetUp {

    @Test
    void shouldGetHeartbeatIntervalWithGetVariablesRequest() {

        int expectedHeartbeatInterval = 100;
        String id = UUID.randomUUID().toString();

        ocppMockServer.expectResponseFromStation(responseWithId(id));

        stationSimulatorRunner.run();

        GetVariableDatum getVariableDatum = new GetVariableDatum()
                .withComponent(new Component().withName(new CiString.CiString50(OCPPCommCtrlrComponent.NAME)))
                .withVariable(new Variable().withName(new CiString.CiString50(HeartbeatIntervalVariableAccessor.NAME)))
                .withAttributeType(GetVariableDatum.AttributeType.ACTUAL);

        Call call = new Call(id, ActionType.GET_VARIABLES, new GetVariablesRequest().withGetVariableData(singletonList(getVariableDatum)));

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            int heartbeatInterval = stationSimulatorRunner.getStation(STATION_ID).getState().getHeartbeatInterval();
            assertThat(heartbeatInterval).isEqualTo(expectedHeartbeatInterval);
            ocppMockServer.verify();
        });
    }
}
