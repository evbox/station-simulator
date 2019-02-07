package com.evbox.everon.ocpp.simulator.message;

import com.evbox.everon.ocpp.simulator.station.exception.ParseException;
import lombok.Value;

import java.io.IOException;
import java.util.Arrays;

@Value
public class CallResult {

    private RawCall rawCall;

    public CallResult(String rawCallJson) {
        this(RawCall.fromJson(rawCallJson));
    }

    public CallResult(RawCall rawCall) {
        if (rawCall.getMessageType() != MessageType.CALL_RESULT) {
            throw new IllegalArgumentException("Expected CALLRESULT message type");
        }
        this.rawCall = rawCall;
    }

    public CallResult(String callId, Object payload) {
        rawCall = new RawCall(Arrays.asList(MessageType.CALL_RESULT.getId(), callId, payload));
    }

    public static CallResult from(RawCall rawCall) {
        return new CallResult(rawCall);
    }

    public static CallResult from(String rawCallJson) {
        return new CallResult(rawCallJson);
    }

    public <T> T getPayload(Class<T> payloadType) {
        Object payload = rawCall.getPayload();
        try {
            String payloadString = ObjectMapperHolder.getJsonObjectMapper().writeValueAsString(payload);
            return ObjectMapperHolder.getJsonObjectMapper().readValue(payloadString, payloadType);
        } catch (IOException e) {
            throw new ParseException("Unable to parse payload of CallResult", e);
        }
    }

    public String toJson() {
        return rawCall.toJson();
    }
}
