package com.evbox.everon.ocpp.simulator.user.interaction;

import com.evbox.everon.ocpp.simulator.cli.ConsoleCommand;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

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
    public void shouldConvertPlugCommandToUserAction() {
        //given
        List<String> commandArgs = singletonList(CONNECTOR_ID.toString());

        //when
        UserAction userAction = ConsoleCommand.toUserAction(PLUG_CMD, commandArgs);

        //then
        assertThat(userAction.getType()).isEqualTo(UserAction.Type.PLUG);
        assertThat(((UserAction.Plug) userAction).getConnectorId()).isEqualTo(1);
    }

    @Test
    public void shouldConvertUnplugCommandToUserAction() {
        //given
        List<String> commandArgs = singletonList(CONNECTOR_ID.toString());

        //when
        UserAction userAction = ConsoleCommand.toUserAction(UNPLUG_CMD, commandArgs);

        //then
        assertThat(userAction.getType()).isEqualTo(UserAction.Type.UNPLUG);
        assertThat(((UserAction.Unplug) userAction).getConnectorId()).isEqualTo(1);
    }

    @Test
    public void shouldConvertAuthCommandToUserAction() {
        //given
        List<String> commandArgs = asList(TOKEN_ID, EVSE_ID.toString());

        //when
        UserAction userAction = ConsoleCommand.toUserAction(AUTH_CMD, commandArgs);

        //then
        assertThat(userAction.getType()).isEqualTo(UserAction.Type.AUTHORIZE);
        assertThat(((UserAction.Authorize) userAction).getTokenId()).isEqualTo(TOKEN_ID);
        assertThat(((UserAction.Authorize) userAction).getEvseId()).isEqualTo(EVSE_ID);
    }

    @Test
    public void shouldValidateConnectorIdForPlugCommand() {
        //given
        String nonNumericConnectorId = "x";
        List<String> commandArgs = singletonList(nonNumericConnectorId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserAction(PLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected numeric argument at [0], but was '" + nonNumericConnectorId + "'");
    }

    @Test
    public void shouldValidateConnectorIdForUnplugCommand() {
        //given
        String nonNumericConnectorId = "x";
        List<String> commandArgs = singletonList(nonNumericConnectorId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserAction(UNPLUG_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected numeric argument at [0], but was '" + nonNumericConnectorId + "'");
    }

    @Test
    public void shouldValidateAuthKeyCharactersForAuthCommand() {
        //given
        String illegalAuthKey = "12345!";
        List<String> commandArgs = asList(illegalAuthKey, EVSE_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserAction(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected valid 'identifierString' at [0], but was '" + illegalAuthKey + "'");
    }

    @Test
    public void shouldValidateAuthKeyForEmptinessForAuthCommand() {
        //given
        List<String> commandArgs = asList(StringUtils.EMPTY, EVSE_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserAction(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected valid 'identifierString' at [0], but was ''");
    }

    @Test
    public void shouldValidateAuthKeyLengthForAuthCommand() {
        //given
        Random randomizer = new Random();
        String tooLongAuthKey = Stream.generate(() -> TOKEN_ID.charAt(randomizer.nextInt(TOKEN_ID.length()))).map(Objects::toString).limit(37).collect(Collectors.joining());
        List<String> commandArgs = asList(tooLongAuthKey, EVSE_ID.toString());

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserAction(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected valid 'identifierString' at [0], but was '" + tooLongAuthKey + "'");
    }

    @Test
    public void shouldValidateEvseIdForAuthCommand() {
        //given
        String nonNumericEvseId = "x";
        List<String> commandArgs = asList(TOKEN_ID, nonNumericEvseId);

        //when
        Throwable throwable = catchThrowable(() -> ConsoleCommand.toUserAction(AUTH_CMD, commandArgs));

        //then
        assertThat(throwable).hasMessage("Expected numeric argument at [1], but was '" + nonNumericEvseId + "'");
    }
}