package com.evbox.everon.ocpp.simulator.station.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.AUTHORIZED;
import static java.util.Collections.singletonList;

/**
 * When the cable has been plugged but the user is not authorized yet.
 */
@Slf4j
public class WaitingForAuthorizationState implements StationState {

    public static final String NAME = "WAITING_FOR_AUTHORIZATION";

    private StationStateFlowManager stationStateFlowManager;

    @Override
    public void setStationTransactionManager(StationStateFlowManager stationTransactionManager) {
        this.stationStateFlowManager = stationTransactionManager;
    }

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public void onAuthorize(int evseId, String tokenId) {
        StationMessageSender stationMessageSender = stationStateFlowManager.getStationMessageSender();
        StationPersistenceLayer stationPersistenceLayer = stationStateFlowManager.getStationPersistenceLayer();

        log.info("in authorizeToken {}", tokenId);

        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId), (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                Evse evse = stationPersistenceLayer.findEvse(evseId);
                evse.setToken(tokenId);

                if (!evse.hasOngoingTransaction()) {
                    String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                    evse.createTransaction(transactionId);

                    stationMessageSender.sendTransactionEventStart(evseId, AUTHORIZED, tokenId);

                }

                startCharging(stationMessageSender, evse, tokenId);
            }
        });

        stationStateFlowManager.setStateForEvse(evseId, new ChargingState());
    }

    @Override
    public void onUnplug(int evseId, int connectorId) {
        Evse evse = stationStateFlowManager.getStationPersistenceLayer().findEvse(evseId);
        StationMessageSender stationMessageSender = stationStateFlowManager.getStationMessageSender();
        if (evse.findConnector(connectorId).getCableStatus() == CableStatus.LOCKED) {
            throw new IllegalStateException(String.format("Unable to unplug locked connector: %d %d", evseId, connectorId));
        }

        evse.unplug(connectorId);
        evse.clearToken();

        if (evse.hasOngoingTransaction()) {
            evse.stopTransaction();

            stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (request, response) ->
                    stationMessageSender.sendTransactionEventEnded(evse.getId(), connectorId, TransactionEventRequest.TriggerReason.EV_DEPARTED, evse.getStopReason().getStoppedReason()));
        }

        stationStateFlowManager.setStateForEvse(evseId, new AvailableState());
    }

    private void startCharging(StationMessageSender stationMessageSender, Evse evse, String tokenId) {
        Integer connectorId = evse.lockPluggedConnector();
        evse.startCharging();
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, AUTHORIZED, tokenId, TransactionData.ChargingState.CHARGING);
    }

}
