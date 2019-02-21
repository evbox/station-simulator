package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.evse.ConnectorStatus;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.simulator.station.support.CallIdGenerator;
import com.evbox.everon.ocpp.simulator.support.ReflectionUtils;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.station.*;
import com.evbox.everon.ocpp.v20.message.station.TransactionData.ChargingState;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCall;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StationMessageSenderTest {


    @Mock
    StationState stationStateMock;
    @Mock
    SubscriptionRegistry subscriptionRegistryMock;
    @Mock
    WebSocketClient webSocketClientMock;

    StationMessageSender stationMessageSender;

    BlockingQueue<WebSocketClientInboxMessage> queue;

    @BeforeEach
    void setUp() {
        this.queue = new LinkedBlockingQueue<>();
        when(webSocketClientMock.getInbox()).thenReturn(queue);

        ReflectionUtils.injectMock(CallIdGenerator.getInstance(), "callId", ThreadLocal.withInitial(() -> 1));

        this.stationMessageSender = new StationMessageSender(subscriptionRegistryMock, stationStateMock, webSocketClientMock);
    }

    @Test
    void shouldIncreaseMessageIdForConsequentCalls() {

        int numberOfMessages = 3;
        IntStream.range(0, numberOfMessages).forEach(c -> stationMessageSender.sendBootNotification(BootNotificationRequest.Reason.POWER_UP));

        Map<String, Call> sentCalls = stationMessageSender.getSentCalls();

        List<String> actualMessageIds = sentCalls.values().stream().map(Call::getMessageId).collect(toList());

        assertAll(
                () -> assertThat(sentCalls).hasSize(numberOfMessages),
                () -> assertThat(actualMessageIds).containsExactlyInAnyOrder("1", "2", "3")
        );

    }

    @Test
    void verifyTransactionEventStart() throws InterruptedException {

        mockStationState();

        stationMessageSender.sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);

        WebSocketClientInboxMessage actualMessage = queue.poll(100, TimeUnit.MILLISECONDS);

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.STARTED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID))
        );

    }


    @Test
    void verifyTransactionEventUpdate() throws InterruptedException {

        mockStationState();

        stationMessageSender.sendTransactionEventUpdate(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.CHARGING);

        WebSocketClientInboxMessage actualMessage = queue.poll(100, TimeUnit.MILLISECONDS);

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.UPDATED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID))
        );

    }

    @Test
    void verifyTransactionEventEnded() throws InterruptedException {

        mockStationState();

        stationMessageSender.sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS, TriggerReason.AUTHORIZED, TransactionData.StoppedReason.STOPPED_BY_EV);

        WebSocketClientInboxMessage actualMessage = queue.poll(100, TimeUnit.MILLISECONDS);

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.ENDED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getTransactionData().getStoppedReason()).isEqualTo(TransactionData.StoppedReason.STOPPED_BY_EV)
        );

    }

    @Test
    void verifyAuthorize() throws InterruptedException {

        stationMessageSender.sendAuthorizeAndSubscribe(DEFAULT_TOKEN_ID, Collections.singletonList(DEFAULT_EVSE_ID), DEFAULT_SUBSCRIBER);

        WebSocketClientInboxMessage actualMessage = queue.poll(100, TimeUnit.MILLISECONDS);

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        AuthorizeRequest actualPayload = (AuthorizeRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(actualPayload.getEvseId()).containsExactly(DEFAULT_EVSE_ID)
        );
    }

    @Test
    void verifyBootNotification() throws InterruptedException {

        stationMessageSender.sendBootNotification(BootNotificationRequest.Reason.POWER_UP);

        WebSocketClientInboxMessage actualMessage = queue.poll(100, TimeUnit.MILLISECONDS);

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        BootNotificationRequest actualPayload = (BootNotificationRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualPayload.getReason()).isEqualTo(BootNotificationRequest.Reason.POWER_UP),
                () -> assertThat(actualPayload.getChargingStation().getSerialNumber()).isEqualTo(new CiString.CiString20(DEFAULT_SERIAL_NUMBER)),
                () -> assertThat(actualPayload.getChargingStation().getFirmwareVersion()).isEqualTo(new CiString.CiString50(DEFAULT_FIRMWARE_VERSION)),
                () -> assertThat(actualPayload.getChargingStation().getModel()).isEqualTo(new CiString.CiString20(DEFAULT_MODEL))
        );

    }

    @Test
    void verifyStatusNotification() throws InterruptedException {

        when(stationStateMock.getCurrentTime()).thenReturn(new Date().toInstant());
        when(stationStateMock.getConnectorState(anyInt())).thenReturn(ConnectorStatus.UNPLUGGED);

        stationMessageSender.sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS);

        WebSocketClientInboxMessage actualMessage = queue.poll(100, TimeUnit.MILLISECONDS);

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        StatusNotificationRequest actualPayload = (StatusNotificationRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualPayload.getConnectorStatus()).isEqualTo(StatusNotificationRequest.ConnectorStatus.AVAILABLE),
                () -> assertThat(actualPayload.getEvseId()).isEqualTo(DEFAULT_EVSE_ID),
                () -> assertThat(actualPayload.getConnectorId()).isEqualTo(DEFAULT_EVSE_CONNECTORS)
        );

    }

    @Test
    void verifyHeartBeat() throws InterruptedException, JsonProcessingException {

        HeartbeatRequest heartbeatRequest = new HeartbeatRequest();

        stationMessageSender.sendHeartBeatAndSubscribe(heartbeatRequest, DEFAULT_SUBSCRIBER);

        WebSocketClientInboxMessage actualMessage = queue.poll(100, TimeUnit.MILLISECONDS);

        String expectedCall = createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(HEART_BEAT_ACTION)
                .withPayload(new HeartbeatRequest())
                .toJson();

        assertThat(actualMessage.getData().get()).isEqualTo(expectedCall);
    }

    private void mockStationState() {
        when(stationStateMock.getTransactionId(anyInt())).thenReturn(DEFAULT_TRANSACTION_ID);
        when(stationStateMock.getSeqNo(anyInt())).thenReturn(Long.valueOf(DEFAULT_SEQ_NUMBER));
        when(stationStateMock.getCurrentTime()).thenReturn(new Date().toInstant());
    }


}
