package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.station.actions.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.simulator.station.actions.Unplug;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConsoleCommandTest {

    private static final String PLUG_CMD = "plug";
    private static final String UNPLUG_CMD = "unplug";
    private static final String AUTH_CMD = "auth";

    private static final String TOKEN_ID = "045918E24B4D80";
    private static final Integer CONNECTOR_ID = 1;
    private static final Integer EVSE_ID = 1;

    @Test
    void shouldConvertPlugCommandToUserAction() {
        //given
        List<String> commandArgs = singletonList(CONNECTOR_ID.toString());

        //when
        UserMessage userMessage = ConsoleCommand.toUserMessage(PLUG_CMD, commandArgs);

        //then
        assertThat(((Plug) userMessage).getConnectorId()).isEqualTo(1);
    }

    @Test
    void shouldConvertUnplugCommandToUserAction() {
        //given
        List<String> commandArgs = singletonList(CONNECTOR_ID.toString());

        //when
        UserMessage userMessage = ConsoleCommand.toUserMessage(UNPLUG_CMD, commandArgs);

        //then
        assertThat(((Unplug) userMessage).getConnectorId()).isEqualTo(1);
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
    void shouldValidateConnectorIdForPlugCommand() {
        //given
        String nonNumericConnectorId = "x";
        List<String> commandArgs = singletonList(nonNumericConnectorId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(PLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected numeric argument at [0], but was '" + nonNumericConnectorId + "'");
    }

    @Test
    void shouldValidateConnectorIdForUnplugCommand() {
        //given
        String nonNumericConnectorId = "x";
        List<String> commandArgs = singletonList(nonNumericConnectorId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserMessage(UNPLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected numeric argument at [0], but was '" + nonNumericConnectorId + "'");
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
        Random randomizer = new Random();
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
}