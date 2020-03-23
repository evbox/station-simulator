package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.states.helpers.AuthorizeHelper;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.AuthorizeResponse;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.*;
import static java.util.Collections.singletonList;

/**
 * When the cable has been plugged but the user is not authorized yet.
 */
@Slf4j
public class WaitingForAuthorizationState extends AbstractEvseState {

    public static final String NAME = "WAITING_FOR_AUTHORIZATION";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public void onPlug(int evseId, int connectorId) {
        // NOP
    }

    @Override
    public void onAuthorize(int evseId, String tokenId) {
        log.info("in authorizeToken {}", tokenId);

        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId),
                (request, response) -> handleAuthorizeResponse(evseId, tokenId, response));
    }

    private void handleAuthorizeResponse(int evseId, String tokenId, AuthorizeResponse response) {
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        StationStore stationStore = stateManager.getStationStore();
        Evse evse = stationStore.findEvse(evseId);

        if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
            evse.setToken(tokenId);

            if (!evse.hasOngoingTransaction()) {
                String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                evse.createTransaction(transactionId);

                stationMessageSender.sendTransactionEventStart(evseId, AUTHORIZED, tokenId);
            }

            int connectorId = startCharging(evse);
            stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, AUTHORIZED, tokenId, TransactionData.ChargingState.CHARGING);

            stateManager.setStateForEvse(evseId, new ChargingState());
        } else {
            AuthorizeHelper.handleFailedAuthorizeResponse(stateManager, evse);
        }
    }

    @Override
    public void onUnplug(int evseId, int connectorId) {
        Evse evse = stateManager.getStationStore().findEvse(evseId);

        if (evse.findConnector(connectorId).getCableStatus() == CableStatus.LOCKED) {
            throw new IllegalStateException(String.format("Unable to unplug locked connector: %d %d", evseId, connectorId));
        }

        evse.unplug(connectorId);
        evse.clearToken();

        if (evse.hasOngoingTransaction()) {
            evse.stopTransaction();
            StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

            stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (request, response) ->
                    stationMessageSender.sendTransactionEventEnded(evse.getId(), connectorId, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED));
        }

        stateManager.setStateForEvse(evseId, new AvailableState());
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        Evse evse = stationStore.findEvse(evseId);
        evse.setToken(tokenId);

        startCharging(evse);
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connector.getId(), REMOTE_START, tokenId, TransactionData.ChargingState.CHARGING);
        stateManager.setStateForEvse(evseId, new ChargingState());
    }

    @Override
    public void onRemoteStop(int evseId) {
        // NOP
    }

    private int startCharging(Evse evse) {
        Integer connectorId = evse.lockPluggedConnector();
        evse.startCharging();
        return connectorId;
    }
}
