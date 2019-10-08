package com.evbox.everon.ocpp.simulator.station.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.EvseStateManager;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
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
public class WaitingForPlugState implements EvseState {

    public static final String NAME = "WAITING_FOR_PLUG";

    private EvseStateManager evseStateManager;

    @Override
    public void setStationTransactionManager(EvseStateManager stationTransactionManager) {
        this.evseStateManager = stationTransactionManager;
    }

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public void onPlug(int evseId, int connectorId) {
        StationStore stationStore = evseStateManager.getStationStore();
        StationMessageSender stationMessageSender = evseStateManager.getStationMessageSender();

        Evse evse = stationStore.findEvse(evseId);
        if (evse.findConnector(connectorId).getCableStatus() != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %d %d", evseId, connectorId));
        }

        evse.plug(connectorId);

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

        evseStateManager.setStateForEvse(evseId, new ChargingState());
    }

    @Override
    public void onAuthorize(int evseId, String tokenId) {
        StationMessageSender stationMessageSender = evseStateManager.getStationMessageSender();
        StationStore stationStore = evseStateManager.getStationStore();

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

        evseStateManager.setStateForEvse(evseId, new AvailableState());
    }

    private void startCharging(Evse evse) {
        evse.lockPluggedConnector();
        evse.startCharging();
    }

}
