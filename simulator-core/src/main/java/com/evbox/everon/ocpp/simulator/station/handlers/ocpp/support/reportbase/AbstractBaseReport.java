package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZonedDateTime.ofInstant;
import static java.util.Collections.singletonList;

public abstract class AbstractBaseReport {

    protected final StationMessageSender stationMessageSender;
    protected final Clock clock;

    public AbstractBaseReport(StationMessageSender stationMessageSender, Clock clock) {
        this.stationMessageSender = stationMessageSender;
        this.clock = clock;
    }

    public abstract void generateAndRespond(String callId, GetBaseReportRequest request);

    protected void sendNotifyReportRequests(List<ReportDatum> reportData, GetBaseReportRequest request) {
        int size = reportData.size();
        for (int seqNo = 0; seqNo < size; seqNo++) {
            ZonedDateTime now = ofInstant(clock.instant(), clock.getZone());
            stationMessageSender.sendNotifyReport(request.getRequestId(), isLastMessage(seqNo, size), seqNo, now, singletonList(reportData.get(seqNo)));
        }
    }

    private boolean isLastMessage(int seqNo, int size) {
        return seqNo != size - 1;
    }
}
