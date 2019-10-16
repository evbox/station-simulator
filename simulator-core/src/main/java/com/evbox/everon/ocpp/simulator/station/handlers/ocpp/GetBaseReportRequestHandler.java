package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.ConfigurationInventoryReport;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.FullInventoryReport;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.BaseReport;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.SummaryInventoryReport;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;

import java.time.Clock;
import java.util.Map;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest.ReportBase.*;
import static com.google.common.collect.ImmutableMap.of;

public class GetBaseReportRequestHandler implements OcppRequestHandler<GetBaseReportRequest> {

    private final Map<GetBaseReportRequest.ReportBase, BaseReport> reports;

    public GetBaseReportRequestHandler(Clock clock, StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        reports = of(
                FULL_INVENTORY, new FullInventoryReport(stationComponentsHolder, stationMessageSender, clock),
                SUMMARY_INVENTORY, new SummaryInventoryReport(stationComponentsHolder, stationMessageSender, clock),
                CONFIGURATION_INVENTORY, new ConfigurationInventoryReport(stationComponentsHolder, stationMessageSender, clock)
        );
    }

    @Override
    public void handle(String callId, GetBaseReportRequest request) {
        BaseReport report = reports.get(request.getReportBase());
        report.generateAndRespond(callId, request);
    }
}
