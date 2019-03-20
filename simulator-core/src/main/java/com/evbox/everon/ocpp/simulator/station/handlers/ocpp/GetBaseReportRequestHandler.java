package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.ACCEPTED;
import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.NOT_SUPPORTED;
import static java.util.Collections.singletonList;

public class GetBaseReportRequestHandler implements OcppRequestHandler<GetBaseReportRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationComponentsHolder stationComponentsHolder;


    public GetBaseReportRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    @Override
    public void handle(String callId, GetBaseReportRequest request) {
        switch (request.getReportBase()) {
            case FULL_INVENTORY:
                stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
                sendNotifyReportRequests(request.getRequestId(), false);
                break;
            case CONFIGURATION_INVENTORY:
                stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
                sendNotifyReportRequests(request.getRequestId(), true);
                break;
            default: // SUMMARY_INVENTORY
                stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(NOT_SUPPORTED));
        }
    }

    private void sendNotifyReportRequests(int requestId, boolean onlyMutableVariables) {
        List<ReportDatum> reportData = stationComponentsHolder.generateReportData(onlyMutableVariables);

        int size = reportData.size();
        List<Integer> sendOrder = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(sendOrder);

        sendOrder.forEach(seqNo -> {
            stationMessageSender
                    .sendNotifyReport(requestId, seqNo != size - 1, seqNo, singletonList(reportData.get(seqNo)));
        });
    }

}
