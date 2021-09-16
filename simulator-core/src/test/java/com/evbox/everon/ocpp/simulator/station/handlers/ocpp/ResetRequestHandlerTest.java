package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.mock.factory.OcppMessageFactory;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v201.message.centralserver.Reset;
import com.evbox.everon.ocpp.v201.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v201.message.centralserver.ResetResponse;
import com.evbox.everon.ocpp.v201.message.centralserver.ResetStatus;
import com.evbox.everon.ocpp.v201.message.station.BootReason;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.IntStream;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static java.util.stream.Collectors.toList;
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
                .withType(Reset.IMMEDIATE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetStatus.ACCEPTED);

        ArgumentCaptor<AbstractWebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(AbstractWebSocketClientInboxMessage.class);

        verify(stationMessageSender, times(3)).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getAllValues().get(0).getData().get()).isEqualTo(expectedCallResult);
    }

    @Test
    void verifyEventSending() {
        Evse evse = mock(Evse.class);
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(Reset.IMMEDIATE)
                .build();

        when(stationStore.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationStore.hasOngoingTransaction(anyInt())).thenReturn(true);
        when(stationStore.findEvse(anyInt())).thenReturn(evse);
        when(evse.getWattConsumedLastSession()).thenReturn(0L);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationStore).stopCharging(anyInt());
        verify(stationMessageSender).sendTransactionEventEnded(anyInt(), anyInt(),
                any(TriggerReason.class), any(Reason.class), anyLong());

    }

    @Test
    void verifyRebooting() {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(Reset.IMMEDIATE)
                .build();

        when(stationStore.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationStore.hasOngoingTransaction(anyInt())).thenReturn(false);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationStore).clearTokens();
        verify(stationStore).clearTransactions();
        verify(stationMessageSender, times(3)).sendMessage(any(AbstractWebSocketClientInboxMessage.class));
        verify(stationMessageSender).sendBootNotification(any(BootReason.class));

    }

    @Test
    void verifyMessageOnIdleResetRequestType() throws JsonProcessingException {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(Reset.ON_IDLE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetStatus.ACCEPTED);

        ArgumentCaptor<AbstractWebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(AbstractWebSocketClientInboxMessage.class);

        verify(stationMessageSender, times(3)).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getAllValues().get(0).getData().get()).isEqualTo(expectedCallResult);
    }

    @Test
    void verifyRebootingStationWithMultipleEvse() {
        Evse evse = mock(Evse.class);
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(Reset.IMMEDIATE)
                .build();

        when(stationStore.getEvseIds()).thenReturn(IntStream.rangeClosed(1, 3).boxed().collect(toList()));
        when(stationStore.hasOngoingTransaction(anyInt())).thenReturn(true);
        when(stationStore.findEvse(anyInt())).thenReturn(evse);
        when(evse.getWattConsumedLastSession()).thenReturn(0L);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSender, times(3)).sendTransactionEventEnded(anyInt(), anyInt(),
                any(TriggerReason.class), any(Reason.class), anyLong());
        verify(stationMessageSender).sendBootNotification(any(BootReason.class));

    }

    @Test
    void verifyResetEvseOngoingTransaction() {
        Evse evse = mock(Evse.class);
        final int evseId = 1;
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(Reset.IMMEDIATE)
                .withEvse(evseId)
                .build();
        when(stationStore.hasOngoingTransaction(evseId)).thenReturn(true);
        when(stationStore.findEvse(evseId)).thenReturn(evse);
        when(evse.getWattConsumedLastSession()).thenReturn(0L);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);
        verify(stationMessageSender).sendTransactionEventEndedAndSubscribe(anyInt(), anyInt(),
                any(TriggerReason.class), any(Reason.class), anyLong(), any(Subscriber.class));
    }

    @Test
    void verifyResetEvse() {
        final int evseId = 1;
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(Reset.IMMEDIATE)
                .withEvse(evseId)
                .build();
        when(stationStore.hasOngoingTransaction(evseId)).thenReturn(false);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);
        verify(stationStore).clearTokens(evseId);
        verify(stationStore).clearTransactions(evseId);
    }

}
