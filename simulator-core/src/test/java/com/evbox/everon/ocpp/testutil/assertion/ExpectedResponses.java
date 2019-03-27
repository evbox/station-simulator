package com.evbox.everon.ocpp.testutil.assertion;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutil.factory.JsonMessageTypeFactory;

import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * A simple class with factory methods.
 */
public class ExpectedResponses {

    /**
     * Create BootNotificationResponse with default configuration.
     *
     * @return BootNotificationResponse in json.
     */
    public static Function<Call, String> bootNotificationResponse() {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withCurrentTime(ZonedDateTime.now().toString())
                .withIntervalInSeconds(100)
                .withStatus("Accepted")
                .toJson();
    }

    /**
     * Create StatusNotificationResponse with empty payload.
     *
     * @return StatusNotificationResponse in json.
     */
    public static Function<Call, String> statusNotificationResponse() {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withPayload("")
                .toJson();
    }
}
