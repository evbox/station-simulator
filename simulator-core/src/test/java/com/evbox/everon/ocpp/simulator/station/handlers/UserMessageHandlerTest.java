package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserMessageHandlerTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    UserMessage userMessageMock;
    @InjectMocks
    UserMessageHandler userMessageHandler;

    @Test
    void verifyHandlingOfUserMessage() {

        userMessageHandler.handle(userMessageMock);

        ArgumentCaptor<StationState> stationStateCaptor = ArgumentCaptor.forClass(StationState.class);
        ArgumentCaptor<StationMessageSender> stationMessageSenderCaptor = ArgumentCaptor.forClass(StationMessageSender.class);

        verify(userMessageMock).perform(stationStateCaptor.capture(), stationMessageSenderCaptor.capture());

        assertAll(
                () -> assertThat(stationStateMock).isEqualTo(stationStateCaptor.getValue()),
                () -> assertThat(stationMessageSenderMock).isEqualTo(stationMessageSenderCaptor.getValue())
        );
    }
}
