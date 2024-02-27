package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v201.message.station.UpdateFirmwareRequest;
import com.evbox.everon.ocpp.v201.message.station.UpdateFirmwareResponse;
import com.evbox.everon.ocpp.v201.message.station.UpdateFirmwareStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpdateFirmwareMessageHandler implements OcppRequestHandler<UpdateFirmwareRequest> {

    private final StationMessageSender stationMessageSender;

    @Override
    public void handle(String callId, UpdateFirmwareRequest request) {
        stationMessageSender.sendCallResult(callId, new UpdateFirmwareResponse()
                .withStatus(UpdateFirmwareStatus.ACCEPTED));
    }
}
