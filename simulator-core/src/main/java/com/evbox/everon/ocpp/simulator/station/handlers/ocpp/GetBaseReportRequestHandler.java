package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.AbstractBaseReport;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.ConfigurationInventoryReportAbstract;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.FullInventoryReportAbstract;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.SummaryInventoryReportAbstract;
import com.evbox.everon.ocpp.v201.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v201.message.station.ReportBase;

import java.time.Clock;
import java.util.Map;

import static com.evbox.everon.ocpp.v201.message.station.ReportBase.CONFIGURATION_INVENTORY;
import static com.evbox.everon.ocpp.v201.message.station.ReportBase.SUMMARY_INVENTORY;
import static com.google.common.collect.ImmutableMap.of;

public class GetBaseReportRequestHandler implements OcppRequestHandler<GetBaseReportRequest> {

    private final Map<ReportBase, AbstractBaseReport> reports;

    public GetBaseReportRequestHandler(Clock clock, StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        reports = of(
                ReportBase.FULL_INVENTORY, new FullInventoryReportAbstract(stationComponentsHolder, stationMessageSender, clock),
                SUMMARY_INVENTORY, new SummaryInventoryReportAbstract(stationComponentsHolder, stationMessageSender, clock),
                CONFIGURATION_INVENTORY, new ConfigurationInventoryReportAbstract(stationComponentsHolder, stationMessageSender, clock)
        );
    }

    @Override
    public void handle(String callId, GetBaseReportRequest request) {
        AbstractBaseReport report = reports.get(request.getReportBase());
        report.generateAndRespond(callId, request);
    }
}
