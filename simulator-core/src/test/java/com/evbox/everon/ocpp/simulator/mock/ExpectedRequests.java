package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;

import java.util.function.Predicate;

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
     * StatusNotificationRequest with default configuration.
     *
     * @return checks whether an incoming request is StatusNotification or not.
     */
    public static Predicate<Call> statusNotificationRequest() {

        return incomingRequest -> incomingRequest.getActionType() == ActionType.STATUS_NOTIFICATION;
    }


}
