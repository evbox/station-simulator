package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.station.NotifyCentralChargingNeedsRequest;
import com.evbox.everon.ocpp.v20.message.station.NotifyCentralChargingNeedsResponse;

public class NotifyCentralChargingNeedsRequestHandler implements OcppRequestHandler<NotifyCentralChargingNeedsRequest> {

    private final StationMessageSender stationMessageSender;

    public NotifyCentralChargingNeedsRequestHandler(StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
    }

    @Override
    public void handle(String callId, NotifyCentralChargingNeedsRequest request) {
        stationMessageSender.sendCallResult(callId, new NotifyCentralChargingNeedsResponse().withStatus(NotifyCentralChargingNeedsResponse.Status.ACCEPTED));
    }
}
