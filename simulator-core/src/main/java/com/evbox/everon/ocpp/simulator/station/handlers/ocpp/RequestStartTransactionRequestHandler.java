package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.*;

/**
 * Handler for {@link RequestStartTransactionRequest} request.
 */
@Slf4j
public class RequestStartTransactionRequestHandler implements OcppRequestHandler<RequestStartTransactionRequest> {

    private final StationState stationState;
    private final StationMessageSender stationMessageSender;

    public RequestStartTransactionRequestHandler(StationMessageSender stationMessageSender, StationState stationState) {
        this.stationState = stationState;
        this.stationMessageSender = stationMessageSender;
    }

    /**
     * Handle {@link RequestStartTransactionRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, RequestStartTransactionRequest request) {

        Optional<Evse> optionalEvse = Optional.ofNullable(request.getEvseId())
                                        .map(stationState::tryFindEvse)
                                        .orElseGet(stationState::tryFindAvailableEvse);

        if (optionalEvse.isPresent()) {
            Evse evse = optionalEvse.get();
            Connector connector = evse.tryFindAvailableConnector().orElse(null);

            if (connector == null || connector.getCableStatus() != CableStatus.UNPLUGGED) {
                log.debug("Connector not available");
                stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartTransactionResponse.Status.REJECTED));
                return;
            }

            if (!evse.hasOngoingTransaction()) {
                String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                evse.createTransaction(transactionId);
            }

            evse.setToken(request.getIdToken().getIdToken().toString());

            stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartTransactionResponse.Status.ACCEPTED));
            stationMessageSender.sendStatusNotification(evse.getId(), connector.getId(), StatusNotificationRequest.ConnectorStatus.OCCUPIED);
            stationMessageSender.sendTransactionEventStart(evse.getId(), connector.getId(), request.getRemoteStartId(), REMOTE_START);

            startTimeOut(evse.getId(), connector.getId());
        } else {
            log.debug("No available evse to start a new transaction");
            stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartTransactionResponse.Status.REJECTED));
        }
    }

    private void startTimeOut(int evseId, int connectorId) {
        stationState.saveConnectionTimeOutFuture(evseId, Executors.newSingleThreadScheduledExecutor(). schedule(() -> {
            stationMessageSender.sendStatusNotification(evseId, connectorId, StatusNotificationRequest.ConnectorStatus.AVAILABLE);
            stationState.findEvse(evseId).stopTransaction();
            stationMessageSender.sendTransactionEventEnded(evseId, connectorId, null, TransactionData.StoppedReason.TIMEOUT);
        }, stationState.getEVConnectionTimeOut(), TimeUnit.SECONDS));
    }

}
