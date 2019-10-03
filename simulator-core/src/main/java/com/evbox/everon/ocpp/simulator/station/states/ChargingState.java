package com.evbox.everon.ocpp.simulator.station.states;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
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
public class ChargingState implements StationState {

    public static final String NAME =  "CHARGING";

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
        StationPersistenceLayer stationPersistenceLayer = stationStateFlowManager.getStationPersistenceLayer();
        StationMessageSender stationMessageSender = stationStateFlowManager.getStationMessageSender();

        log.info("in authorizeToken {}", tokenId);

        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId), (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                Evse evse = stationPersistenceLayer.findEvse(evseId);

                evse.setToken(tokenId);
                int connectorId = stopCharging(stationMessageSender, evse);

                OptionList<TxStartStopPointVariableValues> stopPoints = stationPersistenceLayer.getTxStopPointValues();
                if (stopPoints.contains(TxStartStopPointVariableValues.AUTHORIZED) && !stopPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                    evse.stopTransaction();
                    evse.clearToken();

                    stationMessageSender.sendTransactionEventEnded(evseId, connectorId, STOP_AUTHORIZED, evse.getStopReason().getStoppedReason());
                }
            }
        });

        stationStateFlowManager.setStateForEvse(evseId, new StoppedState());
    }

    @Override
    public void onRemoteStop(int evseId) {
        Evse evse = stationStateFlowManager.getStationPersistenceLayer().findEvse(evseId);
        StationMessageSender stationMessageSender = stationStateFlowManager.getStationMessageSender();

        evse.remotelyStopCharging();
        Integer connectorId = evse.tryUnlockConnector();
        stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, REMOTE_STOP, TransactionData.ChargingState.EV_DETECTED);

        stationStateFlowManager.setStateForEvse(evseId, new StoppedState());
    }

    private int stopCharging(StationMessageSender stationMessageSender, Evse evse) {
        evse.stopCharging();
        Integer connectorId = evse.unlockConnector();
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, STOP_AUTHORIZED, TransactionData.ChargingState.EV_DETECTED);
        return connectorId;
    }
}
