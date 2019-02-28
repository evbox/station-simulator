package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.AuthorizeResponse;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.AUTHORIZED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.STOP_AUTHORIZED;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

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
     * @param stationState         state of the station
     * @param stationMessageSender event sender of the station
     */
    @Override
    public void perform(StationState stationState, StationMessageSender stationMessageSender) {
        List<Integer> evseIds = singletonList(evseId);
        log.info("in authorizeToken {}", tokenId);

        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, evseIds, (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == IdTokenInfo.Status.ACCEPTED) {
                List<Evse> authorizedEvses = hasEvses(response) ? getEvseList(response, stationState) : singletonList(stationState.getDefaultEvse());

                authorizedEvses.forEach(evse -> evse.setToken(tokenId));

                boolean haveOngoingTransaction = authorizedEvses.stream().allMatch(Evse::hasOngoingTransaction);

                if (!haveOngoingTransaction) {
                    Integer transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                    authorizedEvses.forEach(evse -> evse.createTransaction(transactionId));
                }

                boolean allCharging = authorizedEvses.stream().allMatch(Evse::isCharging);
                boolean allPlugged = authorizedEvses.stream().allMatch(Evse::isCablePlugged);

                if (allCharging) {
                    stopCharging(stationMessageSender, authorizedEvses);
                } else {
                    if (allPlugged) {
                        startCharging(stationMessageSender, authorizedEvses);
                    } else { // !allPlugged && !allCharging
                        if (haveOngoingTransaction) {
                            startCharging(stationMessageSender, authorizedEvses);
                        } else { // !allPlugged && !allCharging && !haveOngoingTransaction
                            authorizedEvses.forEach(evse -> stationMessageSender.sendTransactionEventStart(evse.getId(), AUTHORIZED, tokenId));
                        }
                    }
                }
            }
        });
    }


    private void startCharging(StationMessageSender stationMessageSender, List<Evse> authorizedEvses) {
        authorizedEvses.forEach(evse -> {
            Integer connectorId = evse.lockPluggedConnector();
            evse.startCharging();
            stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, AUTHORIZED, TransactionData.ChargingState.CHARGING);
        });
    }

    private void stopCharging(StationMessageSender stationMessageSender, List<Evse> authorizedEvses) {
        authorizedEvses.forEach(evse -> {
            evse.stopCharging();
            Integer connectorId = evse.unlockConnector();
            stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, STOP_AUTHORIZED, TransactionData.ChargingState.EV_DETECTED);
        });
    }


    private List<Evse> getEvseList(AuthorizeResponse response, StationState stationState) {
        return response.getEvseId().stream().map(stationState::findEvse).collect(toList());
    }

    private boolean hasEvses(AuthorizeResponse response) {
        return response.getEvseId() != null && !response.getEvseId().isEmpty();
    }

}

