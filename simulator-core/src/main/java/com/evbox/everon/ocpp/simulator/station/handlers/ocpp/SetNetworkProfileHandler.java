package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v201.message.station.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SetNetworkProfileHandler implements OcppRequestHandler<SetNetworkProfileRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationStore stationStore;

    @Override
    public void handle(String callId, SetNetworkProfileRequest request) {
        Optional.ofNullable(request.getConnectionData())
                .filter(cd -> OCPPVersion.OCPP_20.equals(cd.getOcppVersion()) && OCPPTransport.JSON.equals(cd.getOcppTransport()))
                .ifPresentOrElse(connectionData -> {
                    stationStore.addNetworkConnectionProfile(request.getConfigurationSlot(), connectionData);
                    stationMessageSender.sendCallResult(callId, new SetNetworkProfileResponse().withStatus(SetNetworkProfileStatus.ACCEPTED));
                },
                () -> {
                    log.debug("Invalid request received!");
                    stationMessageSender.sendCallResult(callId, new SetNetworkProfileResponse().withStatus(SetNetworkProfileStatus.REJECTED));
                });
        }
}
