package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.REMOTE_STOP;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.STOP_AUTHORIZED;
import static java.util.Collections.singletonList;

/**
 *  When the transaction is ongoing and the evse is charging.
 */
@Slf4j
public class ChargingState extends AbstractEvseState {

    public static final String NAME =  "CHARGING";

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
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        log.info("in authorizeToken {}", tokenId);

        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId), (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                Evse evse = stationStore.findEvse(evseId);

                evse.setToken(tokenId);
                int connectorId = stopCharging(stationMessageSender, evse);

                OptionList<TxStartStopPointVariableValues> stopPoints = stationStore.getTxStopPointValues();
                if (stopPoints.contains(TxStartStopPointVariableValues.AUTHORIZED) && !stopPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                    evse.stopTransaction();
                    evse.clearToken();

                    stationMessageSender.sendTransactionEventEnded(evseId, connectorId, STOP_AUTHORIZED, evse.getStopReason().getStoppedReason());
                }
            }
        });

        stateManager.setStateForEvse(evseId, new StoppedState());
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
        Evse evse = stateManager.getStationStore().findEvse(evseId);
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        evse.remotelyStopCharging();
        Integer connectorId = evse.tryUnlockConnector();
        stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, REMOTE_STOP, TransactionData.ChargingState.EV_DETECTED);

        stateManager.setStateForEvse(evseId, new StoppedState());
    }

    private int stopCharging(StationMessageSender stationMessageSender, Evse evse) {
        evse.stopCharging();
        Integer connectorId = evse.unlockConnector();
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, STOP_AUTHORIZED, TransactionData.ChargingState.EV_DETECTED);
        return connectorId;
    }
}
