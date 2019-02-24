package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.message.Call;

import java.time.ZonedDateTime;
import java.util.function.Function;

import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCallResult;

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
        return incomingRequest -> createCallResult()
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
        return incomingRequest -> createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withPayload("")
                .toJson();
    }
}
