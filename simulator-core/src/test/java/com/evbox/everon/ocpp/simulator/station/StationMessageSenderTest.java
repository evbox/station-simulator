package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketMessageInbox;
import com.evbox.everon.ocpp.v201.message.station.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory.createCall;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.LOCKED;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.UNPLUGGED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.OCCUPIED;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationMessageSenderTest {

    private final String STATION_ID = "EVB-P123";

    Connector connector;
    Evse evse;
    StationStore stationStore;
    @Mock
    SubscriptionRegistry subscriptionRegistryMock;
    @Mock
    WebSocketClient webSocketClientMock;

    StationMessageSender stationMessageSender;

    WebSocketMessageInbox queue = new WebSocketMessageInbox();

    @BeforeEach
    void setUp() {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();
        stationConfiguration.setId(STATION_ID);
        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(DEFAULT_EVSE_COUNT);
        evse.setConnectors(DEFAULT_EVSE_CONNECTORS);
        stationConfiguration.setEvse(evse);
        stationStore = new StationStore(stationConfiguration);
        this.connector = stationStore.getDefaultEvse().findConnector(DEFAULT_EVSE_CONNECTORS);
        this.evse = stationStore.getDefaultEvse();
        when(webSocketClientMock.getInbox()).thenReturn(queue);
        this.stationMessageSender = new StationMessageSender(subscriptionRegistryMock, stationStore, webSocketClientMock);
    }

    @Test
    void shouldIncreaseMessageIdForConsequentCalls() {

        int numberOfMessages = 3;
        IntStream.range(0, numberOfMessages).forEach(c -> stationMessageSender.sendBootNotification(BootReason.POWER_UP));

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

        stationMessageSender.sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();
        SignedMeterValue signedMeterValue = actualPayload.getMeterValue().get(0).getSampledValue().get(0).getSignedMeterValue();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEvent.STARTED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionInfo().getTransactionId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(signedMeterValue).isNull()
        );

    }

    @Test
    void verifyTransactionEventStartEichrecht() throws InterruptedException {

        mockStationPersistenceLayer();
        stationStore.setStationId("EVB-Eichrecht");

        stationMessageSender.sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();
        SignedMeterValue signedMeterValue = actualPayload.getMeterValue().get(0).getSampledValue().get(0).getSignedMeterValue();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEvent.STARTED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionInfo().getTransactionId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(signedMeterValue.getSignedMeterData().toString()).isNotEmpty()
        );

    }

    @Test
    void verifyTransactionEventStartISO() throws InterruptedException {

        mockStationPersistenceLayer();
        stationStore.setStationId("ISO-Station");

        stationMessageSender.sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();
        AbstractWebSocketClientInboxMessage actualNextMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());
        Call actualNextCall = Call.fromJson(actualNextMessage.getData().get().toString());

        TransactionEventRequest actualTransactionEventPayload = (TransactionEventRequest) actualCall.getPayload();
        NotifyEVChargingNeedsRequest actualNotifyEVChargingNeedsPayload = (NotifyEVChargingNeedsRequest) actualNextCall.getPayload();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualTransactionEventPayload.getEventType()).isEqualTo(TransactionEvent.STARTED),
                () -> assertThat(actualTransactionEventPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualTransactionEventPayload.getTransactionInfo().getTransactionId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
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
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEvent.UPDATED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionInfo().getTransactionId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID)),
                () -> assertThat(signedMeterValue).isNull()
        );

    }

    @Test
    void verifyTransactionEventEnded() throws InterruptedException {

        mockStationPersistenceLayer();

        stationMessageSender.sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS, TriggerReason.AUTHORIZED, Reason.STOPPED_BY_EV, 0L);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEvent.ENDED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionInfo().getTransactionId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getTransactionInfo().getStoppedReason()).isEqualTo(Reason.STOPPED_BY_EV)
        );
    }

    @Test
    void verifyTransactionEventEndedEichrecht() throws InterruptedException {

        mockStationPersistenceLayer();
        stationStore.setStationId("EVB-Eichrecht");

        stationMessageSender.sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS, TriggerReason.AUTHORIZED, Reason.STOPPED_BY_EV, 0L);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        TransactionEventRequest actualPayload = (TransactionEventRequest) actualCall.getPayload();
        SignedMeterValue signedMeterValue = actualPayload.getMeterValue().get(0).getSampledValue().get(0).getSignedMeterValue();

        assertAll(
                () -> assertThat(actualCall.getMessageId()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(actualPayload.getEventType()).isEqualTo(TransactionEvent.ENDED),
                () -> assertThat(actualPayload.getTriggerReason()).isEqualTo(TriggerReason.AUTHORIZED),
                () -> assertThat(actualPayload.getTransactionInfo().getTransactionId()).isEqualTo(new CiString.CiString36(DEFAULT_TRANSACTION_ID)),
                () -> assertThat(actualPayload.getTransactionInfo().getStoppedReason()).isEqualTo(Reason.STOPPED_BY_EV),
                () -> assertThat(signedMeterValue.getSignedMeterData().toString()).isNotEmpty()
        );
    }

    @Test
    void verifyAuthorize() throws InterruptedException {

        stationMessageSender.sendAuthorizeAndSubscribe(DEFAULT_TOKEN_ID, DEFAULT_SUBSCRIBER);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        AuthorizeRequest actualPayload = (AuthorizeRequest) actualCall.getPayload();

        assertThat(actualPayload.getIdToken().getIdToken()).isEqualTo(new CiString.CiString36(DEFAULT_TOKEN_ID));
    }

    @Test
    void verifyBootNotification() throws InterruptedException {

        stationMessageSender.sendBootNotification(BootReason.POWER_UP);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        BootNotificationRequest actualPayload = (BootNotificationRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualPayload.getReason()).isEqualTo(BootReason.POWER_UP),
                () -> assertThat(actualPayload.getChargingStation().getSerialNumber()).isEqualTo(new CiString.CiString25(DEFAULT_SERIAL_NUMBER)),
                () -> assertThat(actualPayload.getChargingStation().getFirmwareVersion()).isEqualTo(new CiString.CiString50(DEFAULT_FIRMWARE_VERSION)),
                () -> assertThat(actualPayload.getChargingStation().getModel()).isEqualTo(new CiString.CiString20(DEFAULT_MODEL)),
                () -> assertThat(actualPayload.getChargingStation().getVendorName()).isEqualTo(new CiString.CiString50(StationHardwareData.VENDOR_NAME))
        );

    }

    @Test
    void verifyStatusNotification() throws InterruptedException {
        connector.setCableStatus(UNPLUGGED);

        stationMessageSender.sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_EVSE_CONNECTORS);

        AbstractWebSocketClientInboxMessage actualMessage = queue.take();

        Call actualCall = Call.fromJson(actualMessage.getData().get().toString());

        StatusNotificationRequest actualPayload = (StatusNotificationRequest) actualCall.getPayload();

        assertAll(
                () -> assertThat(actualPayload.getConnectorStatus()).isEqualTo(ConnectorStatus.AVAILABLE),
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
        List<ReportData> reportData = singletonList(new ReportData());
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
        connector = new Connector(DEFAULT_CONNECTOR_ID, LOCKED, OCCUPIED);
        evse = new Evse(DEFAULT_EVSE_ID, EvseStatus.UNAVAILABLE, new EvseTransaction(DEFAULT_TRANSACTION_ID, EvseTransactionStatus.IN_PROGRESS), List.of(connector));
        stationStore.setEvses(Map.of(DEFAULT_EVSE_ID, evse));

    }


}
