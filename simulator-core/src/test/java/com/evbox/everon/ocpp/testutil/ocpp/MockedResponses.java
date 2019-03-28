package com.evbox.everon.ocpp.testutil.ocpp;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutil.factory.JsonMessageTypeFactory;

import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * Mocked ocpp responses
 */
public class MockedResponses {

    /**
     * Create BootNotificationResponse with default configuration.
     *
     * @return BootNotificationResponse in json.
     */
    public static Function<Call, String> bootNotificationResponseMock() {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withCurrentTime(ZonedDateTime.now().toString())
                .withIntervalInSeconds(100)
                .withStatus("Accepted")
                .toJson();
    }

    /**
     * Create a response with empty payload.
     *
     * @return response in json.
     */
    public static Function<Call, String> emptyResponse() {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withPayload("")
                .toJson();
    }

    /**
     * Create a HeartbeatResponse with given timestamp.
     *
     * @return response in json.
     */
    public static Function<Call, String> heartbeatResponse(ZonedDateTime serverTime) {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withCurrentTime(serverTime.toString())
                .toJson();
    }

}
