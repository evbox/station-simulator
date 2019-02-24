package com.evbox.everon.ocpp.simulator.mock;

import java.time.ZonedDateTime;

import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCallResult;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_MESSAGE_ID;

/**
 * A simple class with factory methods.
 */
public class ExpectedResponses {

    /**
     * Create BootNotificationResponse with default configuration.
     *
     * @return BootNotificationResponse in json.
     */
    public static String bootNotificationResponse() {
        return createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
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
    public static String statusNotificationResponse() {
        return createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload("")
                .toJson();
    }
}
