package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.NotifyReport;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v201.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v201.message.station.GetBaseReportResponse;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.v201.message.station.GenericDeviceModelStatus.ACCEPTED;
import static com.evbox.everon.ocpp.v201.message.station.ReportBase.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetAbstractBaseReportIt extends StationSimulatorSetUp {

        private static final int NOTIFY_REPORT_VARIABLES_LESS_ONE = 47;
        private static final int BATCH_SIZE = 7;

    @Test
    void shouldReplyToGetBaseReportRequest() {

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(CONFIGURATION_INVENTORY);
        Call call = new Call(DEFAULT_CALL_ID, ActionType.GET_BASE_REPORT, request);

        GetBaseReportResponse response =
                ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), GetBaseReportResponse.class);

        await().untilAsserted(() -> {
            assertThat(response.getStatus()).isEqualTo(ACCEPTED);
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldReplyToGetBaseReportRequestSummary() {

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(SUMMARY_INVENTORY);
        Call call = new Call(DEFAULT_CALL_ID, ActionType.GET_BASE_REPORT, request);

        GetBaseReportResponse response =
                ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), GetBaseReportResponse.class);

        await().untilAsserted(() -> {
            assertThat(response.getStatus()).isEqualTo(ACCEPTED);
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSendNotifyReportOnGetBaseReportRequest() {
        int lastBatch = NOTIFY_REPORT_VARIABLES_LESS_ONE % BATCH_SIZE == 0 ?
                NOTIFY_REPORT_VARIABLES_LESS_ONE / BATCH_SIZE : NOTIFY_REPORT_VARIABLES_LESS_ONE / BATCH_SIZE + 1;
        for (int i = 0; i < lastBatch ; i++) {
            ocppMockServer
                    .when(NotifyReport.request(i, i != lastBatch -1))
                    .thenReturn(NotifyReport.response());
        }

        stationSimulatorRunner.run();

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(FULL_INVENTORY);
        Call call = new Call(DEFAULT_CALL_ID, ActionType.GET_BASE_REPORT, request);

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
