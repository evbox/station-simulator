package com.evbox.everon.ocpp.mock.csms.exchange;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.csms.exchange.Common.emptyResponse;
import static com.evbox.everon.ocpp.mock.csms.exchange.Common.equalsType;
import static com.evbox.everon.ocpp.simulator.message.ActionType.STATUS_NOTIFICATION;

public class StatusNotification {

    /**
     * StatusNotificationRequest with any configuration
     *
     * @return checks whether an incoming request is StatusNotification or not.
     */
    public static Predicate<Call> request() {
        return request -> equalsType(request, STATUS_NOTIFICATION);
    }

    /**
     * StatusNotificationRequest that should have expected status.
     *
     * @return checks whether an incoming request is StatusNotification or not.
     */
    public static Predicate<Call> request(StatusNotificationRequest.ConnectorStatus status) {
        return request -> equalsType(request, STATUS_NOTIFICATION) && equalsStatus(request, status);
    }

    /**
     * Create a response for StatusNotification.
     *
     * @return response in json.
     */
    public static Function<Call, String> response() {
        return emptyResponse();
    }

    private static boolean equalsStatus(Call request, StatusNotificationRequest.ConnectorStatus status) {
        return ((StatusNotificationRequest) request.getPayload()).getConnectorStatus() == status;
    }
}
