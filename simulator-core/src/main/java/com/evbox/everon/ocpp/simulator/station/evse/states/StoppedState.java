package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.AUTHORIZED;
import static java.util.Collections.singletonList;

/**
 * When the evse has been stopped from charging, remotely or locally
 */
@Slf4j
public class StoppedState extends AbstractEvseState {

    public static final String NAME = "STOPPED";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public void onPlug(int evseId, int connectorId) {
        // NOP
    }

    @Override
    public void onUnplug(int evseId, int connectorId) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        Evse evse = stationStore.findEvse(evseId);
        evse.unplug(connectorId);

        stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (request, response) -> {
            OptionList<TxStartStopPointVariableValues> stopPoints = stationStore.getTxStopPointValues();
            if (!stopPoints.contains(TxStartStopPointVariableValues.AUTHORIZED) || stopPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                evse.stopTransaction();
                evse.clearToken();

                stationMessageSender.sendTransactionEventEnded(evseId, connectorId, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
            }
        });

        stateManager.setStateForEvse(evseId, new AvailableState());
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        // NOP
    }

    @Override
    public void onRemoteStop(int evseId) {
        // NOP
    }

    @Override
    public void onAuthorize(int evseId, String tokenId) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        Evse evse = stationStore.findEvse(evseId);

        log.info("in authorizeToken {}", tokenId);
        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId), (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                evse.setToken(tokenId);
                if (!evse.hasOngoingTransaction()) {
                    String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                    evse.createTransaction(transactionId);

                    stationMessageSender.sendTransactionEventStart(evseId, AUTHORIZED, tokenId);
                }

                startCharging(stationMessageSender, evse, tokenId);

                stateManager.setStateForEvse(evseId, new ChargingState());
            }
        });
    }

    private void startCharging(StationMessageSender stationMessageSender, Evse evse, String tokenId) {
        Integer connectorId = evse.lockPluggedConnector();
        evse.startCharging();
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, AUTHORIZED, tokenId, TransactionData.ChargingState.CHARGING);
    }
}
