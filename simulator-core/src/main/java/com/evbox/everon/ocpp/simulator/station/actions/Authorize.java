package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.AUTHORIZED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.STOP_AUTHORIZED;
import static java.util.Collections.singletonList;

/**
 * Represents Authorize message.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class Authorize implements UserMessage {

    private final String tokenId;
    private final Integer evseId;

    /**
     * Perform authorisation logic.
     *
     * @param stationState state of the station
     * @param stationMessageSender event sender of the station
     */
    @Override
    public void perform(StationState stationState, StationMessageSender stationMessageSender) {
        List<Integer> evseIds = singletonList(evseId);
        log.info("in authorizeToken {}", tokenId);

        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, evseIds, (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                List<Integer> authorizedEvseIds = response.getEvseId() == null || response.getEvseId().isEmpty() ? singletonList(stationState.getDefaultEvseId()) : response.getEvseId();
                authorizedEvseIds.forEach(evseId -> stationState.storeToken(evseId, tokenId));

                boolean haveOngoingTransaction = authorizedEvseIds.stream().allMatch(stationState::hasOngoingTransaction);

                if (!haveOngoingTransaction) {
                    Integer transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                    authorizedEvseIds.forEach(evseId -> stationState.findEvse(evseId).setEvseTransaction(new EvseTransaction(transactionId)));
                }

                boolean allCharging = authorizedEvseIds.stream().allMatch(stationState::isCharging);
                boolean allPlugged = authorizedEvseIds.stream().allMatch(stationState::isPlugged);

                if (allPlugged) {
                    startCharging(stationState, stationMessageSender, authorizedEvseIds);
                } else if (allCharging) {
                    stopCharging(stationState, stationMessageSender, authorizedEvseIds);
                } else {
                    if (haveOngoingTransaction) {
                        startCharging(stationState, stationMessageSender, authorizedEvseIds);
                    } else {
                        authorizedEvseIds.forEach(evseId -> stationMessageSender.sendTransactionEventStart(evseId, AUTHORIZED, tokenId));
                    }
                }
            }
        });
    }


    private void startCharging(StationState state, StationMessageSender stationMessageSender, List<Integer> authorizedEvseIds) {
        authorizedEvseIds.forEach(evseId -> {
            Integer connectorId = state.lockConnector(evseId);
            state.startCharging(evseId);
            stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, AUTHORIZED, TransactionData.ChargingState.CHARGING);
        });
    }

    private void stopCharging(StationState state, StationMessageSender stationMessageSender, List<Integer> authorizedEvseIds) {
        authorizedEvseIds.forEach(evseId -> {
            state.stopCharging(evseId);
            Integer connectorId = state.unlockConnector(evseId);
            stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, STOP_AUTHORIZED, TransactionData.ChargingState.EV_DETECTED);
        });
    }
}

