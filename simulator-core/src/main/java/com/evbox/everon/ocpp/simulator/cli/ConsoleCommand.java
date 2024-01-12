package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.station.actions.user.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
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
        private final Pattern identifierStringPattern = Pattern.compile("([a-z]|[A-Z]|[0-9]|\\*|-|_|=|:|\\+|\\||@|\\.){0,36}");

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

            boolean validTokenId = !arg.isEmpty() && identifierStringPattern.matcher(arg).matches();
            checkArgument(validTokenId, "Expected valid 'identifierString' at [%s], but was '%s'", index, arg);
        }
    },

    PROFILE3 {
        private static final int WS_PATH_INDEX = 0;

        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);
            return new SecurityProfile3(args.get(WS_PATH_INDEX));
        }

        @Override
        void validateArgs(List<String> args) {
            validateLength(args, 1);
        }
    },

    FAULT {
        private static final int EVSE_ID_INDEX = 0;
        private static final int CONNECTOR_ID_INDEX = 1;
        private static final int ERROR_CODE_INDEX = 2;
        private static final int ERROR_DESCRIPTION_START_INDEX = 3;

        @Override
        public UserMessage toUserMessage(List<String> args) {
            validateArgs(args);

            String errorDescription = getErrorDescription(args);
            return new Fault(Integer.valueOf(args.get(EVSE_ID_INDEX)), Integer.valueOf(args.get(CONNECTOR_ID_INDEX)), args.get(ERROR_CODE_INDEX), errorDescription);
        }

        @Override
        void validateArgs(List<String> args) {
            checkArgument(args.size() >= 3, "Number of required parameters does not match. Expected at least '%s', actual '%s'", 3, args.size());

            validateEvseAndConnector(args, EVSE_ID_INDEX, CONNECTOR_ID_INDEX);
            validateErrorCode(args, ERROR_CODE_INDEX);
            validateErrorDescription(getErrorDescription(args));
        }

        @Nullable
        private String getErrorDescription(List<String> args) {
            if (args.size() <= ERROR_DESCRIPTION_START_INDEX) {
                return null;
            }

            List<String> errorDescriptionWords = args.subList(ERROR_DESCRIPTION_START_INDEX, args.size());
            return String.join(" ", errorDescriptionWords);
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

    private static void validateErrorCode(List<String> args, int errorCodeIndex) {
        checkArgument(args.size() > errorCodeIndex);

        String errorCode = args.get(errorCodeIndex);
        checkArgument(errorCode.length() <= 50, "Expected error code length to be at most 50, but was '%s'", errorCode.length());
    }

    private static void validateErrorDescription(@Nullable String errorDescription) {
        if (errorDescription == null) {
            return;
        }

        checkArgument(errorDescription.length() <= 500, "Expected error description length to be at most 500, but was '%s'", errorDescription.length());
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
