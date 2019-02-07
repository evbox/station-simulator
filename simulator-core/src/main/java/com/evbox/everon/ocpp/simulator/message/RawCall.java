package com.evbox.everon.ocpp.simulator.message;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Generic structure for CALL, CALLRESULT and CALLERROR data
 */
@Value
public class RawCall {

    private static final Logger LOGGER = LoggerFactory.getLogger(Station.class);

    private static final int MESSAGE_TYPE_INDEX = 0;

    private static final Map<FieldType, Integer> CALL_FIELDS_MAPPING = ImmutableMap.<FieldType, Integer>builder()
            .put(FieldType.MESSAGE_TYPE_ID, MESSAGE_TYPE_INDEX)
            .put(FieldType.MESSAGE_ID, 1)
            .put(FieldType.ACTION_TYPE, 2)
            .put(FieldType.PAYLOAD, 3)
            .build();

    private static final Map<FieldType, Integer> CALL_RESULT_FIELDS_MAPPING = ImmutableMap.<FieldType, Integer>builder()
            .put(FieldType.MESSAGE_TYPE_ID, MESSAGE_TYPE_INDEX)
            .put(FieldType.MESSAGE_ID,1)
            .put(FieldType.PAYLOAD, 2)
            .build();

    private static final Map<FieldType, Integer> CALL_ERROR_FIELDS_MAPPING = ImmutableMap.<FieldType, Integer>builder()
            .put(FieldType.MESSAGE_TYPE_ID, MESSAGE_TYPE_INDEX)
            .put(FieldType.MESSAGE_ID, 1)
            .put(FieldType.ERROR_CODE, 2)
            .put(FieldType.ERROR_DESCRIPTION, 3)
            .put(FieldType.ERROR_DETAILS, 4)
            .build();

    private static final Map<MessageType, Map<FieldType, Integer>> MESSAGE_MAPPING = ImmutableMap.<MessageType,Map<FieldType, Integer>>builder()
            .put(MessageType.CALL, CALL_FIELDS_MAPPING)
            .put(MessageType.CALL_RESULT, CALL_RESULT_FIELDS_MAPPING)
            .put(MessageType.CALL_ERROR, CALL_ERROR_FIELDS_MAPPING)
            .build();

    private final List<Object> fields;

    public RawCall(List<Object> fields) {
        this.fields = fields;
    }

    public static RawCall fromJson(String json) {
        try {
            return new RawCall(ObjectMapperHolder.getJsonObjectMapper().readValue(json, new TypeReference<List<Object>>(){}));
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize call", e);
            throw new DeserializeException(e);
        }
    }

    public static RawCall of(List<Object> fields) {
        return new RawCall(fields);
    }

    public MessageType getMessageType() {
        Object messageTypeId = fields.get(MESSAGE_TYPE_INDEX);
        Preconditions.checkArgument(messageTypeId instanceof Integer,
                "Unable to parse message type: %s", messageTypeId);
        return MessageType.of((Integer) messageTypeId);
    }

    public String getMessageId() {
        return getFieldValue(FieldType.MESSAGE_ID, String.class);
    }

    public Object getPayload() {
        return getFieldValue(FieldType.PAYLOAD, Object.class);
    }

    public String getActionType() {
        return getFieldValue(FieldType.ACTION_TYPE, String.class);
    }

    private <T> T getFieldValue(FieldType fieldType, Class<T> clz) {
        MessageType messageType = getMessageType();

        Object fieldValue = fields.get(MESSAGE_MAPPING.get(messageType).get(fieldType));

        Preconditions.checkArgument(clz.isInstance(fieldValue),
                "Field value type '%s' does not correspond to required '%s'",
                fieldValue.getClass().getName(), clz.getName());

        return (T) fieldValue;
    }

    public String toJson() {
        try {
            return ObjectMapperHolder.getJsonObjectMapper().writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to serialize call", e);
            throw new SerializeException(e);
        }
    }

    @Override
    public String toString() {
        return "RawCall{" + "fields=" + fields + '}';
    }

    enum FieldType {
        MESSAGE_TYPE_ID,
        MESSAGE_ID,
        ACTION_TYPE,
        PAYLOAD,
        ERROR_CODE,
        ERROR_DESCRIPTION,
        ERROR_DETAILS
    }

    static class DeserializeException extends RuntimeException {
        public DeserializeException(Exception e) {
            super(e);
        }
    }

    static class SerializeException extends RuntimeException {
        public SerializeException(Exception e) {
            super(e);
        }
    }
}
