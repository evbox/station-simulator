package com.evbox.everon.ocpp.testutil.ocpp;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.NotifyReportRequest;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;

import java.util.function.Predicate;

/**
 * Default station expected requests.
 */
public class ExpectedRequests {

    /**
     * BootNotificationRequest with any configuration.
     *
     * @return checks whether an incoming request is BootNotification or not.
     */
    public static Predicate<Call> bootNotificationRequest() {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.BOOT_NOTIFICATION;

    }

    /**
     * BootNotificationRequest with given reason.
     *
     * @return checks whether an incoming request is BootNotification or not.
     */
    public static Predicate<Call> bootNotificationRequest(BootNotificationRequest.Reason reason) {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.BOOT_NOTIFICATION &&
                ((BootNotificationRequest) incomingRequest.getPayload()).getReason() == reason;
    }

    /**
     * StatusNotificationRequest that should have expected status.
     *
     * @return checks whether an incoming request is StatusNotification or not.
     */
    public static Predicate<Call> statusNotificationRequest(StatusNotificationRequest.ConnectorStatus expectedStatus) {

        return incomingRequest -> incomingRequest.getActionType() == ActionType.STATUS_NOTIFICATION &&
                ((StatusNotificationRequest) incomingRequest.getPayload()).getConnectorStatus() == expectedStatus;
    }


    /**
     * HeartbeatRequest with any configuration.
     *
     * @@return checks whether an incoming request is HeartbeatRequest or not.
     */
    public static Predicate<Call> heartbeatRequest() {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.HEARTBEAT;
    }

    /**
     * NotifyReportRequest with any configuration.
     *
     * @return checks whether an incoming request is NotifyReportRequest or not.
     */
    public static Predicate<Call> notifyReportRequest() {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.NOTIFY_REPORT;
    }

    /**
     * NotifyReportRequest with given configuration.
     *
     * @return checks whether an incoming request is NotifyReportRequest or not.
     */
    public static Predicate<Call> notifyReportRequest(int seqNo, boolean tbc) {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.NOTIFY_REPORT &&
                ((NotifyReportRequest) incomingRequest.getPayload()).getSeqNo() == seqNo &&
                ((NotifyReportRequest) incomingRequest.getPayload()).getTbc() == tbc;
    }


    /**
     * Authorize request with any configuration.
     *
     * @return checks whether an incoming request is AuthorizeRequest or not.
     */
    public static Predicate<Call> authorizeRequest() {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.AUTHORIZE;

    }

    /**
     * Transaction event with any configuration.
     *
     * @return checks whether an incoming request is TrasanctionEvent or not.
     */
    public static Predicate<Call> transactionEventRequest() {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.TRANSACTION_EVENT;
    }

    /**
     * Transaction event with given type.
     *
     * @return checks whether an incoming request is TrasanctionEvent or not.
     */
    public static Predicate<Call> transactionEventRequest(TransactionEventRequest.EventType type) {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.TRANSACTION_EVENT &&
                ((TransactionEventRequest) incomingRequest.getPayload()).getEventType() == type;
    }

    /**
     * Transaction event with given configuration.
     *
     * @return checks whether an incoming request is TrasanctionEvent or not.
     */
    public static Predicate<Call> transactionEventRequest(TransactionEventRequest.EventType type, int seqNo, String trasanctionId, int evseId) {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.TRANSACTION_EVENT &&
                ((TransactionEventRequest) incomingRequest.getPayload()).getEventType() == type &&
                ((TransactionEventRequest) incomingRequest.getPayload()).getSeqNo() == seqNo &&
                ((TransactionEventRequest) incomingRequest.getPayload()).getTransactionData().getId().toString().equals(trasanctionId) &&
                ((TransactionEventRequest) incomingRequest.getPayload()).getEvse().getId() == evseId;
    }

}
