package com.evbox.everon.ocpp.functional.provisioning;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutil.StationSimulatorSetUp;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.any;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.once;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.notifyReportRequest;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.emptyResponse;
import static com.evbox.everon.ocpp.testutil.station.ExpectedResponses.anyResponse;
import static com.evbox.everon.ocpp.testutil.station.ExpectedResponses.responseWithId;
import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest.ReportBase.CONFIGURATION_INVENTORY;
import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest.ReportBase.FULL_INVENTORY;
import static org.awaitility.Awaitility.await;

public class GetBaseReportTest extends StationSimulatorSetUp {

    private static int NOTIFY_REPORT_VARIABLES = 8;

    @Test
    void shouldReplyToGetBaseReportRequest() {

        String id = UUID.randomUUID().toString();

        mockBootResponses();

        ocppMockServer
                .when(notifyReportRequest(), any())
                .thenReturn(emptyResponse());

        ocppMockServer
                .expectResponseFromStation(responseWithId(id));

        stationSimulatorRunner.run();

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(CONFIGURATION_INVENTORY);
        Call call = new Call(id, ActionType.GET_BASE_REPORT, request);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSendNotifyReportOnGetBaseReportRequest() throws JsonProcessingException {

        mockBootResponses();

        for (int i = 0; i < NOTIFY_REPORT_VARIABLES; i++) {
            ocppMockServer
                    .when(notifyReportRequest(i, true), once())
                    .thenReturn(emptyResponse());
        }

        ocppMockServer
                .when(notifyReportRequest(NOTIFY_REPORT_VARIABLES, false), once())
                .thenReturn(emptyResponse());

        ocppMockServer
                .expectResponseFromStation(anyResponse());

        stationSimulatorRunner.run();

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(FULL_INVENTORY);
        Call call = new Call(UUID.randomUUID().toString(), ActionType.GET_BASE_REPORT, request);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
