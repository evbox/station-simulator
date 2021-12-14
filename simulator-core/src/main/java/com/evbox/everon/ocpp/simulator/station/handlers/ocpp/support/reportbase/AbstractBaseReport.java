package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v201.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v201.message.station.ReportData;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZonedDateTime.ofInstant;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.math.NumberUtils.min;

public abstract class AbstractBaseReport {

    private static final int BATCH_SIZE = 7;
    protected final StationMessageSender stationMessageSender;
    protected final Clock clock;

    public AbstractBaseReport(StationMessageSender stationMessageSender, Clock clock) {
        this.stationMessageSender = stationMessageSender;
        this.clock = clock;
    }

    public abstract void generateAndRespond(String callId, GetBaseReportRequest request);

    protected void sendNotifyReportRequests(List<ReportData> reportData, GetBaseReportRequest request) {
        int size = reportData.size();
        for (int seqNo = 0, startIndex = 0; startIndex < size; seqNo++, startIndex += BATCH_SIZE) {
            ZonedDateTime now = ofInstant(clock.instant(), clock.getZone());
            int endIndex = min(startIndex + BATCH_SIZE, size);
            stationMessageSender.sendNotifyReport(request.getRequestId(), isLastMessage(endIndex, size), seqNo, now, reportData.subList(startIndex, endIndex));
        }
    }

    private boolean isLastMessage(int endIndex, int size) {
        return endIndex != size;
    }
}
