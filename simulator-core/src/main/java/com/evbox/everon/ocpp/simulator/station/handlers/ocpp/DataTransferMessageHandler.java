package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v201.message.centralserver.DataTransferRequest;
import com.evbox.everon.ocpp.v201.message.centralserver.DataTransferResponse;
import com.evbox.everon.ocpp.v201.message.centralserver.DataTransferStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataTransferMessageHandler implements OcppRequestHandler<DataTransferRequest> {

    private final StationMessageSender stationMessageSender;

    @Override
    public void handle(String callId, DataTransferRequest request) {
        stationMessageSender.sendCallResult(callId, new DataTransferResponse()
                .withStatus(DataTransferStatus.ACCEPTED));
    }
}
