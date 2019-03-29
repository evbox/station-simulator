package com.evbox.everon.ocpp.testutil.ocpp;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutil.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.v20.message.station.AuthorizeResponse;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;

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
    public static Function<Call, String> heartbeatResponseMock(ZonedDateTime serverTime) {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withCurrentTime(serverTime.toString())
                .toJson();
    }

    /**
     * Create authorize response with given token id
     * @param tokenId response in json.
     */
    public static Function<Call, String> authorizeResponseMock(String tokenId) {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withPayload(new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)))
                .toJson();
    }

}
