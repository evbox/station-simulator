package com.evbox.everon.ocpp.simulator.configuration;

import com.evbox.everon.ocpp.simulator.RunConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.exception.ConfigurationException;
import com.evbox.everon.ocpp.simulator.message.ObjectMapperHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class ConfigurationPrinter {

    private static final int DEFAULT_FRAME_WIDTH = 80;

    /**
     * Prints configuration to console output.
     * For better readability data is put to a frame with given width.
     * If header does not fit into given width frame expands accordingly.
     *
     * @param runConfiguration arguments passed from command line
     * @param configuration parsed simulator configuration (either from file or from the command line)
     */
    public static void printConfiguration(RunConfiguration runConfiguration, SimulatorConfiguration configuration) {
        String header = makeHeader("Starting with following configuration", DEFAULT_FRAME_WIDTH);
        int headerRelativeWidth = header.length() - 1;
        String config = header;
        config += "\n" + "";
        config += "\n" + ("OCPP URL: " + runConfiguration.getUrl());
        config += "\n" + "";
        config += "\n" + "Console-ready configuration:";
        config += "\n" + toJsonString(configuration, false).replace("\"", "'");
        config += "\n" + "";

        config += "\n" + "YAML configuration:";
        config += Stream.of(toYamlString(configuration).split("\n")).map(str -> "\n" + str).collect(joining());
        config += "\n" + "";

        config += makeBottomBorder(headerRelativeWidth);
        System.out.println(config);
    }

    private static String makeHeader(String str, int width) {
        String leftBorder = "=";
        String rightBorder = "=";

        String title = " " + str + " ";
        int requiredWidth = title.length() + leftBorder.length() + rightBorder.length();
        int finalWidth = Math.max(requiredWidth, width);

        int paddingSize = (finalWidth - requiredWidth) / 2;

        String leftPadding = StringUtils.repeat('=', paddingSize);
        String rightPadding = StringUtils.repeat('=', paddingSize);

        if (requiredWidth + paddingSize * 2 < width) {
            rightPadding += "=";
        }

        return "\n" + leftBorder + leftPadding + title + rightPadding + rightBorder;
    }

    private static String makeBottomBorder(int width) {
        String leftBorder = "=";
        String rightBorder = "=";

        String padding = StringUtils.repeat('=', width - leftBorder.length() - rightBorder.length());
        return "\n" + leftBorder + padding + rightBorder;
    }

    private static String toJsonString(SimulatorConfiguration configuration, boolean pretty) {
        try {
            ObjectMapper objectMapper = ObjectMapperHolder.JSON_OBJECT_MAPPER;
            return pretty ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configuration) : objectMapper.writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Unable to serialize configuration to JSON");
        }
    }

    private static String toYamlString(SimulatorConfiguration configuration) {
        try {
            return ObjectMapperHolder.YAML_OBJECT_MAPPER.writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Unable to serialize configuration to YAML");
        }
    }
}
