package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v20.message.station.ConnectionData;
import com.evbox.everon.ocpp.v20.message.station.SetNetworkProfileRequest;
import com.evbox.everon.ocpp.v20.message.station.SetNetworkProfileResponse;
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
        if (isRequestValid(request)) {
            stationStore.addNetworkConnectionProfile(request.getConfigurationSlot(), request.getConnectionData());
            stationMessageSender.sendCallResult(callId, new SetNetworkProfileResponse().withStatus(SetNetworkProfileResponse.Status.ACCEPTED));
        } else {
            log.debug("Invalid request received!");
            stationMessageSender.sendCallResult(callId, new SetNetworkProfileResponse().withStatus(SetNetworkProfileResponse.Status.REJECTED));
        }

    }

    private boolean isRequestValid(SetNetworkProfileRequest request) {
        return Optional.ofNullable(request.getConnectionData()).isPresent()
                && Optional.ofNullable(request.getConnectionData().getOcppVersion()).isPresent()
                && Optional.ofNullable(request.getConnectionData().getOcppTransport()).isPresent()
                && ConnectionData.OcppTransport.JSON.equals(request.getConnectionData().getOcppTransport())
                && ConnectionData.OcppVersion.OCPP_20.equals(request.getConnectionData().getOcppVersion());
    }

}
