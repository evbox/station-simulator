package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.ConfigurationInventoryGenerator;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.FullInventoryGenerator;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.ReportBaseGenerator;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase.SummaryInventoryGenerator;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest.ReportBase.*;
import static com.google.common.collect.ImmutableMap.of;
import static java.time.ZonedDateTime.ofInstant;
import static java.util.Collections.singletonList;

public class GetBaseReportRequestHandler implements OcppRequestHandler<GetBaseReportRequest> {

    private final StationMessageSender stationMessageSender;
    private final Clock clock;
    private final Map<GetBaseReportRequest.ReportBase, ReportBaseGenerator> generators;

    public GetBaseReportRequestHandler(Clock clock, StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.clock = clock;

        generators = of(
                FULL_INVENTORY, new FullInventoryGenerator(stationComponentsHolder, stationMessageSender),
                SUMMARY_INVENTORY, new SummaryInventoryGenerator(stationMessageSender),
                CONFIGURATION_INVENTORY, new ConfigurationInventoryGenerator(stationComponentsHolder, stationMessageSender)
        );
    }

    @Override
    public void handle(String callId, GetBaseReportRequest request) {
        sendNotifyReportRequests(generators.get(request.getReportBase()).generateAndRespond(callId, request), request);
    }

    private void sendNotifyReportRequests(List<ReportDatum> reportData, GetBaseReportRequest request) {
        int size = reportData.size();
        for (int seqNo = 0; seqNo < size; seqNo++) {
            ZonedDateTime now = ofInstant(clock.instant(), clock.getZone());
            stationMessageSender.sendNotifyReport(request.getRequestId(), seqNo != size - 1, seqNo, now, singletonList(reportData.get(seqNo)));
        }
    }

}
