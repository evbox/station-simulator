package com.evbox.everon.ocpp.mock.csms.exchange;

import com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.csms.exchange.Common.equalsType;
import static com.evbox.everon.ocpp.simulator.message.ActionType.BOOT_NOTIFICATION;

public class BootNotification {

    public static final int DEFAULT_HEARTBEAT_INTERVAL = 100;

    /**
     * BootNotificationRequest with given reason.
     *
     * @param reason reason for sending the signal
     * @return checks whether an incoming request is BootNotification or not.
     */
    public static Predicate<Call> request(BootNotificationRequest.Reason reason) {
        return request -> equalsType(request, BOOT_NOTIFICATION) && equalsReason(request, reason);
    }

    /**
     * Create BootNotificationResponse with default configuration.
     *
     * @return BootNotificationResponse in json.
     */
    public static Function<Call, String> response() {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withCurrentTime(ZonedDateTime.now(ZoneOffset.UTC).toString())
                .withIntervalInSeconds(DEFAULT_HEARTBEAT_INTERVAL)
                .withStatus("Accepted")
                .toJson();
    }

    private static boolean equalsReason(Call request, BootNotificationRequest.Reason reason) {
        return ((BootNotificationRequest) request.getPayload()).getReason() == reason;
    }
}
