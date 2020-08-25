package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.station.UnlockConnectorRequest;
import com.evbox.everon.ocpp.v201.message.station.UnlockConnectorResponse;
import com.evbox.everon.ocpp.v201.message.station.UnlockStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Handler for {@link UnlockConnectorRequest} request.
 */
@Slf4j
public class UnlockConnectorRequestHandler implements OcppRequestHandler<UnlockConnectorRequest> {

    private final StationStore stationStore;
    private final StationMessageSender stationMessageSender;

    public UnlockConnectorRequestHandler(StationStore stationStore, StationMessageSender stationMessageSender) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
    }

    /**
     * Handle {@link UnlockConnectorRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, UnlockConnectorRequest request) {
        int evseId = request.getEvseId();
        Optional<Evse> evse = stationStore.tryFindEvse(evseId);
        if (evse.isPresent() && !evse.get().hasOngoingTransaction()) {
            evse.get().tryUnlockConnector();
            stationMessageSender.sendCallResult(callId, new UnlockConnectorResponse().withStatus(UnlockStatus.UNLOCKED));
        } else {
            log.debug("Received UnlockConnectorRequest with invalid evseId: " + evseId);
            stationMessageSender.sendCallResult(callId, new UnlockConnectorResponse().withStatus(UnlockStatus.UNLOCK_FAILED));
        }
    }
}
