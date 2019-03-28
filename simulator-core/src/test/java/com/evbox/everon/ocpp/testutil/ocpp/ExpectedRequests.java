package com.evbox.everon.ocpp.testutil.ocpp;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.NotifyReportRequest;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;

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
     * StatusNotificationRequest that should have expected status.
     *
     * @return checks whether an incoming request is StatusNotification or not.
     */
    public static Predicate<Call> statusNotificationRequestWithStatus(StatusNotificationRequest.ConnectorStatus expectedStatus) {

        return incomingRequest -> {
            if (incomingRequest.getActionType() == ActionType.STATUS_NOTIFICATION) {
                return StatusNotificationRequest.class.cast(incomingRequest.getPayload()).getConnectorStatus() == expectedStatus;
            }
            return false;
        };
    }

    /**
     * HeartbeatRequest with any configuration.
     * @@return checks whether an incoming request is HeartbeatRequest or not.
     */
    public static Predicate<Call> heartbeatRequest() {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.HEARTBEAT;
    }

    /**
     * NotifyReportRequest with any configuration.
     * @return checks whether an incoming request is NotifyReportRequest or not.
     */
    public static Predicate<Call> notifyReportRequest() {
        return incomingRequest -> incomingRequest.getActionType() == ActionType.NOTIFY_REPORT;
    }

    /**
     * NotifyReportRequest with given configuration.
     * @return checks whether an incoming request is NotifyReportRequest or not.
     */
    public static Predicate<Call> notifyReportRequest(int seqNo, boolean tbc) {
        return incomingRequest -> {
            if (incomingRequest.getActionType() == ActionType.NOTIFY_REPORT) {
                NotifyReportRequest request = NotifyReportRequest.class.cast(incomingRequest.getPayload());
                return seqNo == request.getSeqNo() && tbc == request.getTbc();
            }
            return false;
        };
    }

}
