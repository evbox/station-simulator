package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.station.actions.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.simulator.station.actions.Unplug;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Contains list of console commands representing user actions applicable to certain station.
 * For each command there is a validation logic and mapping to {@link UserMessage}
 */
public enum ConsoleCommand {

    PLUG {
        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);
            return new Plug(Integer.valueOf(args.get(0)));
        }

        @Override
        void validateArgs(List<String> args) {
            validateLength(args, 1);

            int connectorIdArgIndex = 0;
            validateNumeric(connectorIdArgIndex, args.get(connectorIdArgIndex));
        }
    },

    UNPLUG {
        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);
            return new Unplug(Integer.valueOf(args.get(0)));
        }

        @Override
        void validateArgs(List<String> args) {
            validateLength(args, 1);

            int connectorIdArgIndex = 0;
            validateNumeric(connectorIdArgIndex, args.get(connectorIdArgIndex));
        }
    },

    AUTH {
        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);
            return new Authorize(args.get(0), Integer.valueOf(args.get(1)));
        }

        @Override
        void validateArgs(List<String> args) {
            validateLength(args, 2);

            int authKeyArgIndex = 0;
            int connectorIdArgIndex = 1;
            validateIdentifierString(authKeyArgIndex, args.get(authKeyArgIndex));
            validateNumeric(connectorIdArgIndex, args.get(connectorIdArgIndex));
        }
    };

    public static boolean contains(String commandName) {
        return toEnum(commandName).isPresent();
    }

    public static UserMessage toUserMessage(String commandName, List<String> args) {
        return toEnum(commandName).orElseThrow(() -> new IllegalArgumentException("Unknown command: " + commandName)).toUserMessage(args);
    }

    private static Optional<ConsoleCommand> toEnum(String commandName) {
        return Stream.of(values()).filter(val -> val.name().equalsIgnoreCase(commandName)).findAny();
    }

    private static void validateIdentifierString(int index, String arg) {
        boolean validTokenId = !arg.isEmpty() && arg.matches("([a-z]|[A-Z]|[0-9]|\\*|-|_|=|:|\\+|\\||@|\\.){0,36}");
        Preconditions.checkArgument(validTokenId, "Expected valid 'identifierString' at [%s], but was '%s'", index, arg);
    }

    private static void validateNumeric(int index, String arg) {
        Preconditions.checkArgument(StringUtils.isNumeric(arg), "Expected numeric argument at [%s], but was '%s'", index, arg);
    }

    private static void validateLength(List<String> args, int expectedLength) {
        Preconditions.checkArgument(args.size() == expectedLength,
                "Number of required parameters does not match. Expected '%s', actual '%s'", expectedLength, args.size());
    }

    public abstract UserMessage toUserMessage(List<String> args);

    abstract void validateArgs(List<String> args);
}
