package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse;

import java.time.Clock;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.NOT_SUPPORTED;

public class SummaryInventoryReport extends BaseReport {

    public SummaryInventoryReport(StationMessageSender stationMessageSender, Clock clock) {
        super(stationMessageSender, clock);
    }

    @Override
    public void generateAndRespond(String callId, GetBaseReportRequest request) {
        stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(NOT_SUPPORTED));
    }
}
