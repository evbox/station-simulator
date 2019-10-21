package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.*;
import static java.util.Collections.singletonList;

/**
 * When the user has been authorized, but the cable is not plugged yet.
 */
@Slf4j
public class WaitingForPlugState extends AbstractEvseState {

    public static final String NAME = "WAITING_FOR_PLUG";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public void onPlug(int evseId, int connectorId) {
        StationStore stationStore = stateManager.getStationStore();

        Evse evse = stationStore.findEvse(evseId);
        if (evse.findConnector(connectorId).getCableStatus() != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %d %d", evseId, connectorId));
        }

        evse.plug(connectorId);

        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (statusNotificationRequest, statusNotificationResponse) -> {
            String tokenId = evse.getTokenId();
            log.info("Station has authorised token {}", tokenId);

            if (!evse.hasOngoingTransaction()) {
                String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                evse.createTransaction(transactionId);

                stationMessageSender.sendTransactionEventStart(evseId, connectorId, CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
            } else {
                stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, tokenId, TransactionData.ChargingState.EV_DETECTED);
            }

            startCharging(evse);

            stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, TransactionEventRequest.TriggerReason.CHARGING_STATE_CHANGED, tokenId,
                    TransactionData.ChargingState.CHARGING);
        });

        stateManager.setStateForEvse(evseId, new ChargingState());
    }

    @Override
    public void onAuthorize(int evseId, String tokenId) {
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        StationStore stationStore = stateManager.getStationStore();

        log.info("in authorizeToken {}", tokenId);

        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId), (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                Evse evse = stationStore.findEvse(evseId);

                if (evse.hasOngoingTransaction()) {
                    evse.stopTransaction();
                    evse.clearToken();

                    stationMessageSender.sendTransactionEventEnded(evse.getId(), null, TransactionEventRequest.TriggerReason.EV_DEPARTED, evse.getStopReason().getStoppedReason());
                }
            }
        });

        stateManager.setStateForEvse(evseId, new AvailableState());
    }

    @Override
    public void onUnplug(int evseId, int connectorId) {
        // NOP
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        // NOP
    }

    @Override
    public void onRemoteStop(int evseId) {
        // NOP
    }

    private void startCharging(Evse evse) {
        evse.lockPluggedConnector();
        evse.startCharging();
    }

}
