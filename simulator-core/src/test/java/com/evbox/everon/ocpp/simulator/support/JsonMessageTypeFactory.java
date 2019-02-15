package com.evbox.everon.ocpp.simulator.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;
import static java.util.Objects.nonNull;

public class JsonMessageTypeFactory {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModules(getJavaTimeModule());

    public static Call createCall() {
        return new Call();
    }

    public static CallResult createCallResult() {
        return new CallResult();
    }

    public static CallError createCallError() {
        return new CallError();
    }

    public static class Call {

        private String messageId;
        private String action;
        private Object payload;

        public Call withMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Call withAction(String action) {
            this.action = action;
            return this;
        }

        public Call withPayload(Object payload) {
            this.payload = payload;
            return this;
        }

        public String toJson() throws JsonProcessingException {
            String payloadJson = JSON_OBJECT_MAPPER.writeValueAsString(payload);

            return "[2,\"" + messageId + "\",\"" + action + "\"," + payloadJson + "]";
        }

    }

    public static class CallResult {

        private String messageId;
        private String currentTime;
        private int intervalInSeconds;
        private String status;
        private Object payload;

        public CallResult withMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public CallResult withCurrentTime(String currentTime) {
            this.currentTime = currentTime;
            return this;
        }

        public CallResult withIntervalInSeconds(int intervalInSeconds) {
            this.intervalInSeconds = intervalInSeconds;
            return this;
        }

        public CallResult withStatus(String status) {
            this.status = status;
            return this;
        }

        public CallResult withPayload(Object payload) {
            this.payload = payload;
            return this;
        }

        public String toJson() throws JsonProcessingException {

            if (nonNull(payload)) {
                String payloadJson = JSON_OBJECT_MAPPER.writeValueAsString(payload);

                return "[3,\"" + messageId + "\"," + payloadJson + "]";
            }

            return "[3, \"" + messageId + "\", {\"currentTime\":\"" + currentTime + "\", \"interval\":" + intervalInSeconds + ", \"status\":\"" + status + "\"}]";
        }

    }

    public static class CallError {

        private String messageId;
        private String errorCode;
        private String errorDescription;

        public CallError withMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public CallError withErrorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public CallError withErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
            return this;
        }

        public String toJson() {
            return "[4, \"" + messageId + "\", \"" + errorCode + "\", \":" + errorDescription + "\", {}]";
        }
    }

    private static JavaTimeModule getJavaTimeModule() {
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendLiteral('T')
                .append(new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2)
                        .appendLiteral(':')
                        .appendValue(MINUTE_OF_HOUR, 2)
                        .optionalStart()
                        .appendLiteral(':')
                        .appendValue(SECOND_OF_MINUTE, 2)
                        .optionalStart()
                        .appendFraction(MILLI_OF_SECOND, 0, 3, true)
                        .toFormatter())
                .appendOffsetId()
                .toFormatter();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(dateTimeFormatter));
        return javaTimeModule;
    }

}
