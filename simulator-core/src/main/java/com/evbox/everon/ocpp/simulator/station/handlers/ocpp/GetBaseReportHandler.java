package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse;
import com.google.common.collect.ImmutableList;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.ACCEPTED;
import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.NOT_SUPPORTED;
import static com.google.common.collect.ImmutableList.of;

public class GetBaseReportHandler implements OcppRequestHandler<GetBaseReportRequest> {

    private static final ImmutableList<String> SUPPORTED_VARIABLES = of("HeartbeatInterval");
    private static final Integer UNKONWN_ID = Integer.valueOf(0);

    private final StationMessageSender stationMessageSender;
    private final StationComponentsHolder stationComponentsHolder;


    public GetBaseReportHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    @Override
    public void handle(String callId, GetBaseReportRequest request) {
        switch (request.getReportBase()) {
            case FULL_INVENTORY:
                stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
            case CONFIGURATION_INVENTORY:
                stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
            default:
                stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(NOT_SUPPORTED));

        }
    }
}
