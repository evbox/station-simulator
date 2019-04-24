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

public class GetVariablesIt extends StationSimulatorSetUp {

    @Test
    void shouldReplyToGetVariablesRequest() {

        int expectedNumberOfVariables = 1;

        stationSimulatorRunner.run();

        GetVariablesRequest getVariablesRequest =
                createGetVariablesRequest(OCPPCommCtrlrComponent.NAME, HeartbeatIntervalVariableAccessor.NAME, GetVariableDatum.AttributeType.ACTUAL);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.GET_VARIABLES, getVariablesRequest);

        ocppMockServer.waitUntilConnected();


        GetVariablesResponse response =
                ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), GetVariablesResponse.class);

        await().untilAsserted(() -> {
            assertThat(response.getGetVariableResult().size()).isEqualTo(expectedNumberOfVariables);
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldGetHeartbeatIntervalWithGetVariablesRequest() {

        int expectedHeartbeatInterval = 100;

        stationSimulatorRunner.run();

        GetVariablesRequest getVariablesRequest =
                createGetVariablesRequest(OCPPCommCtrlrComponent.NAME, HeartbeatIntervalVariableAccessor.NAME, GetVariableDatum.AttributeType.ACTUAL);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.GET_VARIABLES, getVariablesRequest);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            int heartbeatInterval = stationSimulatorRunner.getStation(STATION_ID).getStateView().getHeartbeatInterval();
            assertThat(heartbeatInterval).isEqualTo(expectedHeartbeatInterval);
            ocppMockServer.verify();
        });
    }

    GetVariablesRequest createGetVariablesRequest(String component, String variable, GetVariableDatum.AttributeType attributeType) {
        GetVariableDatum getVariableDatum = new GetVariableDatum()
                .withComponent(new Component().withName(new CiString.CiString50(component)))
                .withVariable(new Variable().withName(new CiString.CiString50(variable)))
                .withAttributeType(attributeType);

        return new GetVariablesRequest().withGetVariableData(singletonList(getVariableDatum));
    }
}
