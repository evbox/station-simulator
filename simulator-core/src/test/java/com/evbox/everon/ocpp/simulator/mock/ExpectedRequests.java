package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;

import java.util.function.Predicate;

import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.UNAVAILABLE;

/**
 * Default station expected requests.
 */
public class ExpectedRequests {

    /**
     * BootNotificationRequest with default configuration.
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

}
