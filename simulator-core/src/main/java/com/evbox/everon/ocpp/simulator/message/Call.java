package com.evbox.everon.ocpp.simulator.message;

import com.evbox.everon.ocpp.simulator.station.exceptions.ParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;

import java.io.IOException;
import java.util.Arrays;

@Value
public class Call {

    private final RawCall rawCall;

    public Call(String messageId, ActionType actionType, Object payload) {
        rawCall = new RawCall(Arrays.asList(MessageType.CALL.getId(), messageId, actionType.getType(), payload));
    }

    public static Call fromJson(String json) {
        return Call.from(RawCall.fromJson(json));
    }

    public static Call from(RawCall rawCall) {
        if (rawCall.getMessageType() != MessageType.CALL) {
            throw new IllegalArgumentException("Expected CALL message type: " + rawCall.getMessageType());
        }

        ActionType actionType = ActionType.of(rawCall.getActionType());
        Object rawPayload = rawCall.getPayload();
        Object typedPayload;

        try {
            ObjectMapper objectMapper = ObjectMapperHolder.JSON_OBJECT_MAPPER;
            typedPayload = objectMapper.readValue(objectMapper.writeValueAsString(rawPayload), actionType.getRequestType());
        } catch (IOException e) {
            throw new ParseException("Unable to parse request call payload", e);
        }

        return new Call(rawCall.getMessageId(), actionType, typedPayload);
    }

    public String getMessageId() {
        return rawCall.getMessageId();
    }

    public ActionType getActionType() {
        return ActionType.of(rawCall.getActionType());
    }

    public MessageType getMessageType() {
        return rawCall.getMessageType();
    }

    public Object getPayload() {
        return rawCall.getPayload();
    }

    public String toJson() {
        return rawCall.toJson();
    }

    @Override
    public String toString() {
        return "Call{" + "rawCall=" + rawCall + '}';
    }

}
