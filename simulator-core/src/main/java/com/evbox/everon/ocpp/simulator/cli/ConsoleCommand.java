package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.station.actions.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.simulator.station.actions.Unplug;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Contains list of console commands representing user actions applicable to certain station.
 * For each command there is a validation logic and mapping to {@link UserMessage}
 */
public enum ConsoleCommand {

    PLUG {
        private static final int EVSE_ID_INDEX = 0;
        private static final int CONNECTOR_ID_INDEX = 1;

        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);
            return new Plug(Integer.valueOf(args.get(EVSE_ID_INDEX)), Integer.valueOf(args.get(CONNECTOR_ID_INDEX)));
        }

        @Override
        void validateArgs(List<String> args) {
            validateLength(args, 2);

            validateEvseAndConnector(args, EVSE_ID_INDEX, CONNECTOR_ID_INDEX);
        }
    },

    UNPLUG {
        private static final int EVSE_ID_INDEX = 0;
        private static final int CONNECTOR_ID_INDEX = 1;

        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);
            return new Unplug(Integer.valueOf(args.get(EVSE_ID_INDEX)), Integer.valueOf(args.get(CONNECTOR_ID_INDEX)));
        }

        @Override
        void validateArgs(List<String> args) {
            validateLength(args, 2);

            validateEvseAndConnector(args, EVSE_ID_INDEX, CONNECTOR_ID_INDEX);
        }
    },

    AUTH {
        private static final int RFID_INDEX = 0;
        private static final int EVSE_ID_INDEX = 1;

        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);
            return new Authorize(args.get(RFID_INDEX), Integer.valueOf(args.get(EVSE_ID_INDEX)));
        }

        @Override
        void validateArgs(List<String> args) {
            validateLength(args, 2);

            validateIdentifierString(RFID_INDEX, args.get(RFID_INDEX));
            validateNumeric(EVSE_ID_INDEX, args.get(EVSE_ID_INDEX));
        }

        private void validateIdentifierString(int index, String arg) {
            boolean validTokenId = !arg.isEmpty() && arg.matches("([a-z]|[A-Z]|[0-9]|\\*|-|_|=|:|\\+|\\||@|\\.){0,36}");
            checkArgument(validTokenId, "Expected valid 'identifierString' at [%s], but was '%s'", index, arg);
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

    private static void validateEvseAndConnector(List<String> args, int evseIdArgIndex, int connectorIdArgIndex) {
        checkArgument(args.size() > connectorIdArgIndex);

        validateNumeric(evseIdArgIndex, args.get(evseIdArgIndex));
        validateNumeric(connectorIdArgIndex, args.get(connectorIdArgIndex));
    }

    private static void validateNumeric(int index, String arg) {
        checkArgument(StringUtils.isNumeric(arg), "Expected numeric argument at [%s], but was '%s'", index, arg);
    }

    private static void validateLength(List<String> args, int expectedLength) {
        checkArgument(args.size() == expectedLength,
                "Number of required parameters does not match. Expected '%s', actual '%s'", expectedLength, args.size());
    }

    public abstract UserMessage toUserMessage(List<String> args);

    abstract void validateArgs(List<String> args);
}
