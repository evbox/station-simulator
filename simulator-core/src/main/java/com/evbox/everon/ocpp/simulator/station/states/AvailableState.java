package com.evbox.everon.ocpp.simulator.station.states;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.*;
import com.evbox.everon.ocpp.simulator.station.actions.system.CancelRemoteStartTransaction;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.AUTHORIZED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.REMOTE_START;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * When the station is ready for an authorize or a plug
 */
@Slf4j
public class AvailableState implements EvseState {

    public static final String NAME = "AVAILABLE";

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
        Evse evse = evseStateManager.getStationStore().findEvse(evseId);

        if (evse.findConnector(connectorId).getCableStatus() != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %d %d", evseId, connectorId));
        }

        StationMessageSender stationMessageSender = evseStateManager.getStationMessageSender();

        evse.plug(connectorId);
        stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (statusNotificationRequest, statusNotificationResponse) -> {

            OptionList<TxStartStopPointVariableValues> startPoints = stationStore.getTxStartPointValues();
            if (startPoints.contains(TxStartStopPointVariableValues.EV_CONNECTED) && !startPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                evse.createTransaction(transactionId);

                stationMessageSender.sendTransactionEventStart(evseId, connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
            }
        });

        evseStateManager.setStateForEvse(evseId, new WaitingForAuthorizationState());
    }

    @Override
    public void onAuthorize(int evseId, String tokenId) {
        StationMessageSender stationMessageSender = evseStateManager.getStationMessageSender();
        StationStore stationStore = evseStateManager.getStationStore();

        log.info("in authorizeToken {}", tokenId);

        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId), (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                List<Evse> authorizedEvses = hasEvses(response) ? getEvseList(response, stationStore) : singletonList(stationStore.getDefaultEvse());

                authorizedEvses.forEach(evse -> evse.setToken(tokenId));

                OptionList<TxStartStopPointVariableValues> startPoints = stationStore.getTxStartPointValues();
                if (startPoints.contains(TxStartStopPointVariableValues.AUTHORIZED) && !startPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                    String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                    authorizedEvses.forEach(evse -> evse.createTransaction(transactionId));

                    authorizedEvses.forEach(evse -> stationMessageSender.sendTransactionEventStart(evse.getId(), AUTHORIZED, tokenId));
                }
            }
        });

        evseStateManager.setStateForEvse(evseId, new WaitingForPlugState());
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {

        StationStore stationStore = evseStateManager.getStationStore();
        StationMessageSender stationMessageSender = evseStateManager.getStationMessageSender();

        Evse evse = stationStore.findEvse(evseId);

        String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
        evse.createTransaction(transactionId);

        evse.setToken(tokenId);

        stationMessageSender.sendStatusNotification(evse.getId(), connector.getId(), StatusNotificationRequest.ConnectorStatus.OCCUPIED);
        stationMessageSender.sendTransactionEventStart(evse.getId(), connector.getId(), remoteStartId, REMOTE_START);

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            Station station = evseStateManager.getStation();
            station.sendMessage(new StationMessage(station.getConfiguration().getId(), StationMessage.Type.SYSTEM_ACTION, new CancelRemoteStartTransaction(evseId, connector.getId())));
        }, stationStore.getEVConnectionTimeOut(), TimeUnit.SECONDS);

        evseStateManager.setStateForEvse(evseId, new WaitingForPlugState());
    }

    private List<Evse> getEvseList(AuthorizeResponse response, StationStore stationStore) {
        return response.getEvseId().stream().map(stationStore::findEvse).collect(toList());
    }

    private boolean hasEvses(AuthorizeResponse response) {
        return response.getEvseId() != null && !response.getEvseId().isEmpty();
    }
}
