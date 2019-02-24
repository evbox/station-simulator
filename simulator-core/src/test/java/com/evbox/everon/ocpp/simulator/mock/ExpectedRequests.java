package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.support.builders.BootNotificationRequestBuilder;

import java.util.function.Predicate;

import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCall;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.BOOT_NOTIFICATION_ACTION;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_MESSAGE_ID;

/**
 * Default station expected requests.
 */
public class ExpectedRequests {

    /**
     * BootNotificationRequest with default configuration.
     *
     * @return predicate which does structural equality of incoming request and expected
     */
    public static Predicate<String> bootNotificationRequest() {
        String expectedRequest = createCall()
                .withAction(BOOT_NOTIFICATION_ACTION)
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(BootNotificationRequestBuilder.DEFAULT_INSTANCE)
                .toJson();
        return incomingRequest -> incomingRequest.equals(expectedRequest);
    }

    /**
     * StatusNotificationRequest with default configuration.
     *
     * @return checks whether an incoming request is StatusNotification or not.
     */
    public static Predicate<String> statusNotificationRequest() {

        return incomingRequest -> {
            Call call = Call.fromJson(incomingRequest);
            return call.getActionType() == ActionType.STATUS_NOTIFICATION;
        };
    }


}
