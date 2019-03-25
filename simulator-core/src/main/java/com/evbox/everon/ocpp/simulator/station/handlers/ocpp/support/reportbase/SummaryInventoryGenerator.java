package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;

import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.NOT_SUPPORTED;
import static java.util.Collections.emptyList;

public class SummaryInventoryGenerator extends ReportBaseGenerator {

    private final StationMessageSender stationMessageSender;

    public SummaryInventoryGenerator(StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
    }

    @Override
    public List<ReportDatum> generateAndRespond(String callId, GetBaseReportRequest request) {
        stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(NOT_SUPPORTED));
        return emptyList();
    }
}
