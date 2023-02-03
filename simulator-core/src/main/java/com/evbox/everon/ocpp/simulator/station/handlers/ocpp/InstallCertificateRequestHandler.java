package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v201.message.station.InstallCertificateRequest;
import com.evbox.everon.ocpp.v201.message.station.InstallCertificateResponse;
import com.evbox.everon.ocpp.v201.message.station.InstallCertificateStatus;
import com.evbox.everon.ocpp.v201.message.station.StatusInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InstallCertificateRequestHandler implements OcppRequestHandler<InstallCertificateRequest> {

    private final StationMessageSender stationMessageSender;

    @Override
    public void handle(String callId, InstallCertificateRequest request) {
        log.info("received {}, responding with a dummy status", request);

        sendResponse(callId, new InstallCertificateResponse()
                .withStatus(InstallCertificateStatus.REJECTED)
                .withStatusInfo(new StatusInfo()
                        .withReasonCode(new CiString.CiString20("Not Implemented"))
                        .withAdditionalInfo(new CiString.CiString512("Dummy Handler"))));
    }

    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new AbstractWebSocketClientInboxMessage.OcppMessage(callStr));
    }
}
