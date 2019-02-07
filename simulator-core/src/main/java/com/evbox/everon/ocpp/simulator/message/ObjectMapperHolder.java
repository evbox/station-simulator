package com.evbox.everon.ocpp.simulator.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;

@FieldDefaults(makeFinal = true)
public class ObjectMapperHolder {

    private static DateTimeFormatter OCPP_ZONED_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
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

    private static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModules(getJavaTimeModule());

    private static ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    public static ObjectMapper getJsonObjectMapper() {
        return JSON_OBJECT_MAPPER;
    }

    public static ObjectMapper getYamlObjectMapper() {
        return YAML_OBJECT_MAPPER;
    }

    private ObjectMapperHolder() {
    }

    private static JavaTimeModule getJavaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(OCPP_ZONED_DATE_TIME_FORMATTER));
        return javaTimeModule;
    }
}
