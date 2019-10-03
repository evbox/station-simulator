package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationDataHolder;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.RequestStopTransactionRequest;
import com.evbox.everon.ocpp.v20.message.station.RequestStopTransactionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Handler for {@link RequestStopTransactionRequest} request.
 */
@Slf4j
public class RequestStopTransactionRequestHandler implements OcppRequestHandler<RequestStopTransactionRequest> {

    private final StationPersistenceLayer stationPersistenceLayer;
    private final StationMessageSender stationMessageSender;
    private final StationStateFlowManager stationStateFlowManager;

    public RequestStopTransactionRequestHandler(StationDataHolder stationDataHolder) {
        this.stationPersistenceLayer = stationDataHolder.getStationPersistenceLayer();
        this.stationMessageSender = stationDataHolder.getStationMessageSender();
        this.stationStateFlowManager = stationDataHolder.getStationStateFlowManager();
    }

    /**
     * Handle {@link RequestStopTransactionRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, RequestStopTransactionRequest request) {

        String transactionId = request.getTransactionId().toString();
        Optional<Evse> evse = stationPersistenceLayer.tryFindEvseByTransactionId(transactionId);
        if (evse.isPresent()) {
            stationMessageSender.sendCallResult(callId, new RequestStopTransactionResponse().withStatus(RequestStopTransactionResponse.Status.ACCEPTED));
            stationStateFlowManager.remoteStop(evse.get().getId());
        } else {
            log.debug("Received RequestStopTransactionRequest with invalid transactionID: " + transactionId);
            stationMessageSender.sendCallResult(callId, new RequestStopTransactionResponse().withStatus(RequestStopTransactionResponse.Status.REJECTED));
        }
    }
}
