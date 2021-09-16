package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v201.message.station.SendLocalListRequest;
import com.evbox.everon.ocpp.v201.message.station.SendLocalListResponse;
import com.evbox.everon.ocpp.v201.message.station.SendLocalListStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SendLocalListRequestHandler implements OcppRequestHandler<SendLocalListRequest> {

    private final StationMessageSender stationMessageSender;

    @Override
    public void handle(String callId, SendLocalListRequest request) {
        stationMessageSender.sendCallResult(callId, new SendLocalListResponse().withStatus(SendLocalListStatus.ACCEPTED));
    }
}
