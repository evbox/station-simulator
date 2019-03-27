package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.testutil.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.testutil.factory.OcppMessageFactory;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetResponse;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResetRequestHandlerTest {

    @Mock
    StationMessageSender stationMessageSender;
    @Mock
    StationState stationState;

    @InjectMocks
    ResetRequestHandler resetRequestHandler;

    @Test
    void verifyMessageOnImmediateResetRequestType() throws JsonProcessingException {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.IMMEDIATE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetResponse.Status.ACCEPTED);

        ArgumentCaptor<WebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResult);
    }

    @Test
    void verifyEventSending() {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.IMMEDIATE)
                .build();

        when(stationState.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationState.hasOngoingTransaction(anyInt())).thenReturn(true);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationState).stopCharging(anyInt());
        verify(stationMessageSender).sendTransactionEventEndedAndSubscribe(anyInt(), anyInt(),
                any(TransactionEventRequest.TriggerReason.class), any(TransactionData.StoppedReason.class), any(Subscriber.class));

    }

    @Test
    void verifyRebooting() {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.IMMEDIATE)
                .build();

        when(stationState.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationState.hasOngoingTransaction(anyInt())).thenReturn(false);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationState).clearTokens();
        verify(stationState).clearTransactions();
        verify(stationMessageSender, times(3)).sendMessage(any(WebSocketClientInboxMessage.class));
        verify(stationMessageSender).sendBootNotification(any(BootNotificationRequest.Reason.class));

    }

    @Test
    void verifyMessageOnIdleResetRequestType() throws JsonProcessingException {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.ON_IDLE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetResponse.Status.REJECTED);

        ArgumentCaptor<WebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResult);
    }

}
