package com.evbox.everon.ocpp.testutils.ocpp.exchange;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;

import java.util.function.Predicate;

import static com.evbox.everon.ocpp.simulator.message.ActionType.TRANSACTION_EVENT;

public class TransactionEvent extends Exchange {

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
    public static Predicate<Call> request(TransactionEventRequest.EventType type) {
        return request -> equalsType(request, TRANSACTION_EVENT) && equalsEventType(request, type);
    }

    /**
     * Transaction event with given configuration.
     *
     * @param type          transaction type
     * @param seqNo         sequence number
     * @param transactionId id of the transaction
     * @param evseId        evse id
     * @return checks whether an incoming request is TransactionEvent or not.
     */
    public static Predicate<Call> request(TransactionEventRequest.EventType type, int seqNo, String transactionId, int evseId) {
        return request -> equalsType(request, TRANSACTION_EVENT) &&
                equalsEventType(request, type) &&
                equalsSeqNo(request, seqNo) &&
                equalsTransactionId(request, transactionId) &&
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
    public static Predicate<Call> request(TransactionEventRequest.EventType type, int seqNo, String transactionId, int evseId, String tokenId) {
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
     * @param transactionId id of the transaction
     * @param evseId        evse id
     * @param tokenId       id of the auth token
     * @param chargingState charging satte
     * @param triggerReason trigger reason for the event
     * @return checks whether an incoming request is TransactionEvent or not.
     */
    public static Predicate<Call> request(
            TransactionEventRequest.EventType type,
            int seqNo,
            String transactionId,
            int evseId,
            String tokenId,
            TransactionData.ChargingState chargingState,
            TransactionEventRequest.TriggerReason triggerReason) {

        return request -> equalsType(request, TRANSACTION_EVENT) &&
                equalsEventType(request, type) &&
                equalsSeqNo(request, seqNo) &&
                equalsTransactionId(request, transactionId) &&
                equalsEvseId(request, evseId) &&
                equalsTokenId(request, tokenId) &&
                equalsChargingState(request, chargingState) &&
                equalsTriggerReason(request, triggerReason);
    }

    private static boolean equalsEventType(Call request, TransactionEventRequest.EventType type) {
        return ((TransactionEventRequest) request.getPayload()).getEventType() == type;
    }

    private static boolean equalsSeqNo(Call request, int seqNo) {
        return ((TransactionEventRequest) request.getPayload()).getSeqNo() == seqNo;
    }

    private static boolean equalsTransactionId(Call request, String transactionId) {
        return ((TransactionEventRequest) request.getPayload()).getTransactionData().getId().toString().equals(transactionId);
    }

    private static boolean equalsEvseId(Call request, int evseId) {
        return ((TransactionEventRequest) request.getPayload()).getEvse().getId() == evseId;
    }

    private static boolean equalsTokenId(Call request, String tokenId) {
        return ((TransactionEventRequest) request.getPayload()).getIdToken().getIdToken().toString().equals(tokenId);
    }

    private static boolean equalsChargingState(Call request, TransactionData.ChargingState chargingState) {
        return ((TransactionEventRequest) request.getPayload()).getTransactionData().getChargingState() == chargingState;
    }

    private static boolean equalsTriggerReason(Call request, TransactionEventRequest.TriggerReason triggerReason) {
        return ((TransactionEventRequest) request.getPayload()).getTriggerReason() == triggerReason;
    }
}

