package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.mock.factory.OcppMessageFactory;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
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

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResetRequestHandlerTest {

    @Mock
    StationMessageSender stationMessageSender;
    @Mock
    StationStore stationStore;

    @InjectMocks
    ResetRequestHandler resetRequestHandler;

    @Test
    void verifyMessageOnImmediateResetRequestType() throws JsonProcessingException {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.IMMEDIATE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetResponse.Status.ACCEPTED);

        ArgumentCaptor<AbstractWebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(AbstractWebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResult);
    }

    @Test
    void verifyEventSending() {
        Evse evse = mock(Evse.class);
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.IMMEDIATE)
                .build();

        when(stationStore.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationStore.hasOngoingTransaction(anyInt())).thenReturn(true);
        when(stationStore.findEvse(anyInt())).thenReturn(evse);
        when(evse.getWattConsumedLastSession()).thenReturn(0L);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationStore).stopCharging(anyInt());
        verify(stationMessageSender).sendTransactionEventEndedAndSubscribe(anyInt(), anyInt(),
                any(TransactionEventRequest.TriggerReason.class), any(TransactionData.StoppedReason.class), anyLong(), any(Subscriber.class));

    }

    @Test
    void verifyRebooting() {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.IMMEDIATE)
                .build();

        when(stationStore.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationStore.hasOngoingTransaction(anyInt())).thenReturn(false);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationStore).clearTokens();
        verify(stationStore).clearTransactions();
        verify(stationMessageSender, times(3)).sendMessage(any(AbstractWebSocketClientInboxMessage.class));
        verify(stationMessageSender).sendBootNotification(any(BootNotificationRequest.Reason.class));

    }

    @Test
    void verifyMessageOnIdleResetRequestType() throws JsonProcessingException {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetRequest.Type.ON_IDLE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetResponse.Status.ACCEPTED);

        ArgumentCaptor<AbstractWebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(AbstractWebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResult);
    }

}
