package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketMessageInbox;
import com.evbox.everon.ocpp.v20.message.common.SignedMeterValue;
import com.evbox.everon.ocpp.v20.message.station.*;
import com.evbox.everon.ocpp.v20.message.station.TransactionData.ChargingState;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.factory.EvseCreator.createEvse;
import static com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory.createCall;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationMessageSenderTest {

    private final String STATION_ID = "EVB-P123";

    @Mock
    StationStore stationStoreMock;
    @Mock
    SubscriptionRegistry subscriptionRegistryMock;
    @Mock
    WebSocketClient webSocketClientMock;

    StationMessageSender stationMessageSender;

    WebSocketMessageInbox queue = new WebSocketMessageInbox();

    @BeforeEach
    void setUp() {
        when(webSocketClientMock.getInbox()).thenReturn(queue);
        this.stationMessageSender = new StationMessageSender(subscriptionRegistryMock, stationStoreMock, webSocketClientMock);
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

        mockStationPersistenceLayer();

        stationMessageSender.sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();
        SignedMeterValue signedMeterValue = actualPayload.getMeterValue().get(0).getSampledValue().get(0).getSignedMeterValue();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.STARTED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(signedMeterValue).isNull()
        );

    }

    @Test
    void verifyTransactionEventStartEichrecht() throws InterruptedException {

        mockStationPersistenceLayer();
        when(stationStoreMock.getStationId()).thenReturn("EVB-Eichrecht");

        stationMessageSender.sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();
        SignedMeterValue signedMeterValue = actualPayload.getMeterValue().get(0).getSampledValue().get(0).getSignedMeterValue();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.STARTED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(signedMeterValue.getEncodedMeterValue().toString()).isNotEmpty()
        );

    }

    @Test
    void verifyTransactionEventStartISO() throws InterruptedException {

        mockStationPersistenceLayer();
        when(stationStoreMock.getStationId()).thenReturn("ISO-Station");

        stationMessageSender.sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();
        AbstractWebSocketClientInboxMessage actualNextMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());
        Call actualNextCall = Call.fromJson(actualNextMessage.getData().get().toString());

        TransactionEventRequest actualTransactionEventPayload = (TransactionEventRequest) actualCall.getPayload();
        NotifyEVChargingNeedsRequest actualNotifyEVChargingNeedsPayload = (NotifyEVChargingNeedsRequest) actualNextCall.getPayload();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualTransactionEventPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.STARTED),
                () -> assertThat(actualTransactionEventPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualTransactionEventPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualTransactionEventPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(actualNotifyEVChargingNeedsPayload.getEvseId()).isEqualTo(DEFAULT_EVSE_ID),
                () -> assertThat(actualNotifyEVChargingNeedsPayload.getChargingNeeds()).isNotNull()
        );

    }


    @Test
    void verifyTransactionEventUpdate() throws InterruptedException {

        mockStationPersistenceLayer();

        stationMessageSender.sendTransactionEventUpdate(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.CHARGING);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();
        SignedMeterValue signedMeterValue = actualPayload.getMeterValue().get(0).getSampledValue().get(0).getSignedMeterValue();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.UPDATED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(signedMeterValue).isNull()
        );

    }

    @Test
    void verifyTransactionEventEnded() throws InterruptedException {

        mockStationPersistenceLayer();

        stationMessageSender.sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS, TriggerReason.AUTHORIZED, TransactionData.StoppedReason.STOPPED_BY_EV, 0L);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

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
    void verifyTransactionEventEndedEichrecht() throws InterruptedException {

        mockStationPersistenceLayer();
        when(stationStoreMock.getStationId()).thenReturn("EVB-Eichrecht");

        stationMessageSender.sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS, TriggerReason.AUTHORIZED, TransactionData.StoppedReason.STOPPED_BY_EV, 0L);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();
        SignedMeterValue signedMeterValue = actualPayload.getMeterValue().get(0).getSampledValue().get(0).getSignedMeterValue();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.ENDED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionData().getId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getTransactionData().getStoppedReason()).isEqualTo(TransactionData.StoppedReason.STOPPED_BY_EV),
                () -> assertThat(signedMeterValue.getEncodedMeterValue().toString()).isNotEmpty()
        );
    }

    @Test
    void verifyAuthorize() throws InterruptedException {

        stationMessageSender.sendAuthorizeAndSubscribe(DEFAULT_TOKEN_ID, singletonList(DEFAULT_EVSE_ID), DEFAULT_SUBSCRIBER);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

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

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

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

        when(stationStoreMock.getCurrentTime()).thenReturn(new Date().toInstant());
        Evse evse = mock(Evse.class, RETURNS_DEEP_STUBS);
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evse);
        when(evse.findConnector(anyInt()).getCableStatus()).thenReturn(CableStatus.UNPLUGGED);

        stationMessageSender.sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

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

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        String expectedCall = createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(HEART_BEAT_ACTION)
                .withPayload(new HeartbeatRequest())
                .toJson();

        assertThat(actualMessage.getData().get()).isEqualTo(expectedCall);
    }

    @Test
    void verifyNotifyReportAsync() throws InterruptedException, JsonProcessingException {
        List<ReportDatum> reportData = singletonList(new ReportDatum());
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        NotifyReportRequest request = new NotifyReportRequest()
                .withTbc(false)
                .withSeqNo(0)
                .withReportData(reportData)
                .withGeneratedAt(now);

        stationMessageSender.sendNotifyReport(null, false, 0, now, reportData);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        String expectedCall = createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(NOTIFY_REPORT_ACTION)
                .withPayload(request)
                .toJson();

        assertThat(actualMessage.getData().get()).isEqualTo(expectedCall);
    }

    private void mockStationPersistenceLayer() {
        Evse evse = createEvse()
                .withTransaction(new EvseTransaction(DEFAULT_TRANSACTION_ID, EvseTransactionStatus.IN_PROGRESS))
                .withId(DEFAULT_EVSE_ID)
                .build();
        when(stationStoreMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);
        when(stationStoreMock.getStationId()).thenReturn(STATION_ID);

        when(stationStoreMock.getCurrentTime()).thenReturn(new Date().toInstant());
    }


}
