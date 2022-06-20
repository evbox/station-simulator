package com.evbox.everon.ocpp.mock.csms.exchange;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v201.message.station.ChargingState;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TransactionEventRequest;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.csms.exchange.Common.emptyResponse;
import static com.evbox.everon.ocpp.mock.csms.exchange.Common.equalsType;
import static com.evbox.everon.ocpp.simulator.message.ActionType.TRANSACTION_EVENT;

public class TransactionEvent {

    /**
     * Transaction event with any configuration.
     *
     * @return checks whether an incoming request is TrasanctionEvent or not.
     */
    public static Predicate<Call> request() {
        return request -> equalsType(request, TRANSACTION_EVENT);
    }

    /**
     * Transaction event with given type.
     *
     * @param type transaction type
     * @return checks whether an incoming request is TrasanctionEvent or not.
     */
    public static Predicate<Call> request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent type) {
        return request -> equalsType(request, TRANSACTION_EVENT) && equalsEventType(request, type);
    }

    /**
     * Transaction event with given configuration.
     *
     * @param type          transaction type
     * @param stoppedReason stopped reason
     * @return checks whether an incoming request is TrasanctionEvent or not.
     */
    public static Predicate<Call> request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent type, Reason stoppedReason) {
        return request -> equalsType(request, TRANSACTION_EVENT) &&
                equalsEventType(request, type) &&
                equalsStoppedReason(request, stoppedReason);
    }

    /**
     * Transaction event with given configuration.
     *
     * @param type          transaction type
     * @param chargingState charging state
     * @param triggerReason trigger reason
     * @return checks whether an incoming request is TransactionEvent or not.
     */
    public static Predicate<Call> request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent type, ChargingState chargingState, TriggerReason triggerReason) {
        return request -> equalsType(request, TRANSACTION_EVENT) &&
                equalsEventType(request, type) &&
                equalsChargingState(request, chargingState) &&
                equalsTriggerReason(request, triggerReason);
    }

    /**
     * Transaction event with given configuration.
     *
     * @param type          transaction type
     * @param seqNo         sequence number
     * @param evseId        evse id
     * @return checks whether an incoming request is TransactionEvent or not.
     */
    public static Predicate<Call> request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent type, int seqNo, int evseId) {
        return request -> equalsType(request, TRANSACTION_EVENT) &&
                equalsEventType(request, type) &&
                equalsSeqNo(request, seqNo) &&
                equalsEvseId(request, evseId);
    }

    /**
     * Transaction event with given configuration.
     *
     * @param type          transaction type
     * @param seqNo         sequence number
     * @param transactionId id of the transaction
     * @param evseId        evse id
     * @param tokenId       id of the auth token
     * @return checks whether an incoming request is TransactionEvent or not.
     */
    public static Predicate<Call> request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent type, int seqNo, String transactionId, int evseId, String tokenId) {
        return request -> equalsType(request, TRANSACTION_EVENT) &&
                equalsEventType(request, type) &&
                equalsSeqNo(request, seqNo) &&
                equalsTransactionId(request, transactionId) &&
                equalsEvseId(request, evseId) &&
                equalsTokenId(request, tokenId);
    }

    /**
     * Transaction event with given configuration.
     *
     * @param type          transaction type
     * @param seqNo         sequence number
     * @param evseId        evse id
     * @param tokenId       id of the auth token
     * @param chargingState charging satte
     * @param triggerReason trigger reason for the event
     * @return checks whether an incoming request is TransactionEvent or not.
     */
    public static Predicate<Call> request(
            com.evbox.everon.ocpp.v201.message.station.TransactionEvent type,
            int seqNo,
            int evseId,
            String tokenId,
            ChargingState chargingState,
            TriggerReason triggerReason) {

        return request -> equalsType(request, TRANSACTION_EVENT) &&
                equalsEventType(request, type) &&
                equalsSeqNo(request, seqNo) &&
                equalsEvseId(request, evseId) &&
                equalsTokenId(request, tokenId) &&
                equalsChargingState(request, chargingState) &&
                equalsTriggerReason(request, triggerReason);
    }

    /**
     * Create a response for TransactionEvent.
     *
     * @return response in json.
     */
    public static Function<Call, String> response() {
        return emptyResponse();
    }

    private static boolean equalsEventType(Call request, com.evbox.everon.ocpp.v201.message.station.TransactionEvent type) {
        return ((TransactionEventRequest) request.getPayload()).getEventType() == type;
    }

    private static boolean equalsSeqNo(Call request, int seqNo) {
        return ((TransactionEventRequest) request.getPayload()).getSeqNo() == seqNo;
    }

    private static boolean equalsTransactionId(Call request, String transactionId) {
        return ((TransactionEventRequest) request.getPayload()).getTransactionInfo().getTransactionId().toString().equals(transactionId);
    }

    private static boolean equalsEvseId(Call request, int evseId) {
        return ((TransactionEventRequest) request.getPayload()).getEvse().getId() == evseId;
    }

    private static boolean equalsTokenId(Call request, String tokenId) {
        return ((TransactionEventRequest) request.getPayload()).getIdToken().getIdToken().toString().equals(tokenId);
    }

    private static boolean equalsChargingState(Call request, ChargingState chargingState) {
        return ((TransactionEventRequest) request.getPayload()).getTransactionInfo().getChargingState() == chargingState;
    }

    private static boolean equalsTriggerReason(Call request, TriggerReason triggerReason) {
        return ((TransactionEventRequest) request.getPayload()).getTriggerReason() == triggerReason;
    }

    private static boolean equalsStoppedReason(Call request, Reason stoppedReason) {
        return ((TransactionEventRequest) request.getPayload()).getTransactionInfo().getStoppedReason() == stoppedReason;
    }
}

