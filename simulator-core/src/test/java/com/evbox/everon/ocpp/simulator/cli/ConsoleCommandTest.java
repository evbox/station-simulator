package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.station.actions.user.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.user.Fault;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import com.evbox.everon.ocpp.simulator.station.actions.user.Unplug;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessage;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConsoleCommandTest {

    private static final String PLUG_CMD = "plug";
    private static final String UNPLUG_CMD = "unplug";
    private static final String AUTH_CMD = "auth";
    private static final String FAULT_CMD = "fault";

    private static final String TOKEN_ID = "045918E24B4D80";
    private static final Integer CONNECTOR_ID = 1;
    private static final Integer EVSE_ID = 1;
    private static final String ERROR_CODE = "0x0102";

    private final Random randomizer = new Random();

    @Test
    void shouldConvertPlugCommandToUserAction() {
        //given
        List<String> commandArgs = asList(EVSE_ID.toString(), CONNECTOR_ID.toString());

        //when
        UserMessage userMessage = ConsoleCommand.toUserMessage(PLUG_CMD, commandArgs);

        //then
        assertThat(((Plug) userMessage).getConnectorId()).isEqualTo(CONNECTOR_ID);
        assertThat(((Plug) userMessage).getEvseId()).isEqualTo(EVSE_ID);
    }

    @Test
    void shouldConvertUnplugCommandToUserAction() {
        //given
        List<String> commandArgs = asList(EVSE_ID.toString(), CONNECTOR_ID.toString());

        //when
        UserMessage userMessage = ConsoleCommand.toUserMessage(UNPLUG_CMD, commandArgs);

        //then
        assertThat(((Unplug) userMessage).getConnectorId()).isEqualTo(CONNECTOR_ID);
        assertThat(((Unplug) userMessage).getEvseId()).isEqualTo(EVSE_ID);
    }

    @Test
    void shouldConvertAuthCommandToUserAction() {
        //given
        List<String> commandArgs = asList(TOKEN_ID, EVSE_ID.toString());

        //when
        UserMessage userMessage = ConsoleCommand.toUserMessage(AUTH_CMD, commandArgs);

        //then
        assertThat(((Authorize) userMessage).getTokenId()).isEqualTo(TOKEN_ID);
        assertThat(((Authorize) userMessage).getEvseId()).isEqualTo(EVSE_ID);
    }

    @Test
    void shouldValidateEvseIdForPlugCommand() {
        //given
        String nonNumericEvseId = "x";
        List<String> commandArgs = asList(nonNumericEvseId, CONNECTOR_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(PLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage(formatNumericError(0, nonNumericEvseId));
    }

    @Test
    void shouldValidateConnectorIdForPlugCommand() {
        //given
        String nonNumericConnectorId = "x";
        List<String> commandArgs = asList(EVSE_ID.toString(), nonNumericConnectorId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(PLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage(formatNumericError(1, nonNumericConnectorId));
    }

    @Test
    void shouldValidateEvseIdForUnplugCommand() {
        //given
        String nonNumericEvseId = "x";
        List<String> commandArgs = asList(nonNumericEvseId, CONNECTOR_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(UNPLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage(formatNumericError(0, nonNumericEvseId));
    }

    @Test
    void shouldValidateConnectorIdForUnplugCommand() {
        //given
        String nonNumericConnectorId = "x";
        List<String> commandArgs = asList(EVSE_ID.toString(), nonNumericConnectorId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(UNPLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage(formatNumericError(1, nonNumericConnectorId));
    }

    @Test
    void shouldValidateAuthKeyCharactersForAuthCommand() {
        //given
        String illegalAuthKey = "12345!";
        List<String> commandArgs = asList(illegalAuthKey, EVSE_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected valid 'identifierString' at [0], but was '" + illegalAuthKey + "'");
    }

    @Test
    void shouldValidateAuthKeyForEmptinessForAuthCommand() {
        //given
        List<String> commandArgs = asList(StringUtils.EMPTY, EVSE_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected valid 'identifierString' at [0], but was ''");
    }

    @Test
    void shouldValidateAuthKeyLengthForAuthCommand() {
        //given
        String tooLongAuthKey = Stream.generate(() -> TOKEN_ID.charAt(randomizer.nextInt(TOKEN_ID.length()))).map(Objects::toString).limit(37).collect(Collectors.joining());
        List<String> commandArgs = asList(tooLongAuthKey, EVSE_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected valid 'identifierString' at [0], but was '" + tooLongAuthKey + "'");
    }

    @Test
    void shouldValidateEvseIdForAuthCommand() {
        //given
        String nonNumericEvseId = "x";
        List<String> commandArgs = asList(TOKEN_ID, nonNumericEvseId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected numeric argument at [1], but was '" + nonNumericEvseId + "'");
    }

    @Test
    void shouldConvertFaultCommandToUserAction() {
        //given
        List <String> commandArgs = new ArrayList<>();
        commandArgs.addAll(asList(EVSE_ID.toString(), CONNECTOR_ID.toString(), ERROR_CODE));
        commandArgs.addAll(asList("Auto-recoverable", "DC", "Leakage", "detected."));

        //when
        UserMessage userMessage = ConsoleCommand.toUserMessage(FAULT_CMD, commandArgs);

        //then
        assertThat(((Fault) userMessage).getConnectorId()).isEqualTo(CONNECTOR_ID);
        assertThat(((Fault) userMessage).getEvseId()).isEqualTo(EVSE_ID);
        assertThat(((Fault) userMessage).getErrorCode()).isEqualTo(ERROR_CODE);
        assertThat(((Fault) userMessage).getErrorDescription()).isEqualTo("Auto-recoverable DC Leakage detected.");
    }

    @Test
    void shouldValidateNumberOfArgumentsForFaultCommand() {
        //given
        List<String> commandArgs = asList(EVSE_ID.toString(), CONNECTOR_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(FAULT_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Number of required parameters does not match. Expected at least '3', actual '2'");
    }

    @Test
    void shouldValidateEvseIdForFaultCommand() {
        //given
        String nonNumericEvseId = "x";
        List<String> commandArgs = asList(nonNumericEvseId, CONNECTOR_ID.toString(), ERROR_CODE);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(FAULT_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage(formatNumericError(0, nonNumericEvseId));
    }

    @Test
    void shouldValidateConnectorIdForFaultCommand() {
        //given
        String nonNumericConnectorId = "x";
        List<String> commandArgs = asList(EVSE_ID.toString(), nonNumericConnectorId, ERROR_CODE);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(FAULT_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage(formatNumericError(1, nonNumericConnectorId));
    }

    @Test
    void shouldValidateErrorCodeLengthForFaultCommand() {
        //given
        String tooLongErrorCode = Stream.generate(() -> ERROR_CODE.charAt(randomizer.nextInt(ERROR_CODE.length()))).map(Objects::toString).limit(51).collect(Collectors.joining());
        List<String> commandArgs = asList(EVSE_ID.toString(), CONNECTOR_ID.toString(), tooLongErrorCode);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(FAULT_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected error code length to be at most 50, but was '" + tooLongErrorCode.length() + "'");
    }

    @Test
    void shouldValidateErrorDescriptionLengthForFaultCommand() {
        //given
        String tooLongErrorDescription = Stream.generate(() -> "x").limit(501).collect(Collectors.joining());
        List<String> commandArgs = asList(EVSE_ID.toString(), CONNECTOR_ID.toString(), ERROR_CODE, tooLongErrorDescription);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(FAULT_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected error description length to be at most 500, but was '" + tooLongErrorDescription.length() + "'");
    }

    private static String formatNumericError(int index, String actual) {
        return String.format("Expected numeric argument at [%d], but was '%s'", index, actual);
    }
}