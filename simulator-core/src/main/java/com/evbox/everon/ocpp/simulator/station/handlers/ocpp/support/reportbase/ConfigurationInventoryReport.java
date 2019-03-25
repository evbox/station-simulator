package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;

import java.time.Clock;
import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.ACCEPTED;

public class ConfigurationInventoryReport extends BaseReport {

    private final StationComponentsHolder stationComponentsHolder;

    public ConfigurationInventoryReport(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender, Clock clock) {
        super(stationMessageSender, clock);
        this.stationComponentsHolder = stationComponentsHolder;
    }

    @Override
    public void generateAndRespond(String callId, GetBaseReportRequest request) {
        stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
        List<ReportDatum> reportData = stationComponentsHolder.generateReportData(true);
        sendNotifyReportRequests(reportData, request);
    }
}
