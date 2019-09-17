package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.station.SetChargingProfileRequest;
import com.evbox.everon.ocpp.v20.message.station.SetChargingProfileResponse;

public class SetChargingProfileRequestHandler implements OcppRequestHandler<SetChargingProfileRequest> {

    private final StationMessageSender stationMessageSender;

    public SetChargingProfileRequestHandler(StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
    }

    /**
     * Handle {@link SetChargingProfileRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, SetChargingProfileRequest request) {
        stationMessageSender.sendCallResult(callId, new SetChargingProfileResponse().withStatus(SetChargingProfileResponse.Status.REJECTED));
    }
}
