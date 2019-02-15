package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.actions.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import com.evbox.everon.ocpp.simulator.station.handlers.ServerMessageHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.UserMessageHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StationMessageRouterTest {

    @Mock
    UserMessageHandler userMessageHandlerMock;
    @Mock
    ServerMessageHandler serverMessageHandlerMock;

    @InjectMocks
    StationMessageRouter stationMessageRouter;

    @Captor
    ArgumentCaptor<String> serverMessageCaptor;
    @Captor
    ArgumentCaptor<UserMessage> userMessageCaptor;

    @Test
    void verifyServerMessageRouting() {

        String messageBody = "dummy-body";

        StationMessage stationMessage = new StationMessage(STATION_ID, StationMessage.Type.OCPP_MESSAGE, messageBody);

        stationMessageRouter.route(stationMessage);

        verify(serverMessageHandlerMock).handle(serverMessageCaptor.capture());

        assertThat(messageBody).isEqualTo(serverMessageCaptor.getValue());

    }

    @Test
    void verifyUserMessageRouting() {

        UserMessage userMessage = new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID);

        StationMessage stationMessage = new StationMessage(STATION_ID, StationMessage.Type.USER_ACTION, userMessage);

        stationMessageRouter.route(stationMessage);

        verify(userMessageHandlerMock).handle(userMessageCaptor.capture());

        assertThat(userMessage).isEqualTo(userMessageCaptor.getValue());

    }
}
