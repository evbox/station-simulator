package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserMessageHandlerTest {

    @Mock
    StateManager stateManagerMock;
    @Mock
    UserMessage userMessageMock;
    @InjectMocks
    UserMessageHandler userMessageHandler;

    @Test
    void verifyHandlingOfUserMessage() {

        userMessageHandler.handle(userMessageMock);

        ArgumentCaptor<StateManager> stationTransactionsManagerCaptor = ArgumentCaptor.forClass(StateManager.class);

        verify(userMessageMock).perform(stationTransactionsManagerCaptor.capture());

        assertThat(stateManagerMock).isEqualTo(stationTransactionsManagerCaptor.getValue());
    }
}
