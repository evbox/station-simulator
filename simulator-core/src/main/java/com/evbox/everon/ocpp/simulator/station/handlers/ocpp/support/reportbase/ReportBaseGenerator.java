package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;

import java.util.List;

public abstract class ReportBaseGenerator {

    public abstract List<ReportDatum> generateAndRespond(String callId, GetBaseReportRequest request);
}
