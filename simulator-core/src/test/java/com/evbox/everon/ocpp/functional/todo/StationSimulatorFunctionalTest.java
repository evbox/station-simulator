package com.evbox.everon.ocpp.functional.todo;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.message.ObjectMapperHolder;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.actions.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.simulator.station.actions.Unplug;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import com.evbox.everon.ocpp.testutil.remove.WebSocketServerMock;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v20.message.common.IdToken;
import com.evbox.everon.ocpp.v20.message.station.*;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutil.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.testutil.factory.SimulatorConfigCreator.createStationConfiguration;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


public class StationSimulatorFunctionalTest {

    private WebSocketServerMock server = new WebSocketServerMock("/ocpp", 0);

    private StationSimulatorRunner stationSimulatorRunner;

    private String ocppServerUrl;

    @BeforeEach
    void setUp() {
        Awaitility.setDefaultTimeout(60, TimeUnit.SECONDS);
        server.start();
        ocppServerUrl = "ws://localhost:" + server.getPort() + "/ocpp";

        SimulatorConfiguration.StationConfiguration stationConfiguration = createStationConfiguration(STATION_ID, 1, 1);
        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(stationConfiguration);

        stationSimulatorRunner = new StationSimulatorRunner(ocppServerUrl, simulatorConfiguration);

        mockSuccessfulStatusNotificationAnswer();
    }

    @Test
    void shouldSendConnectorStatusAndTransactionStartedWhenCablePluggedIn() {
        //given
        mockSuccessfulBootNotificationAnswer();
        stationSimulatorRunner.run();

        //when
        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        //then
        await().untilAsserted(() -> {

            List<Call> stationCalls = server.getReceivedCalls(STATION_ID);

            Optional<Call> statusNotificationCallOptional = stationCalls.stream()
                    .filter(call -> call.getActionType() == ActionType.STATUS_NOTIFICATION)
                    .filter(call -> ((StatusNotificationRequest) call.getPayload()).getConnectorStatus() == StatusNotificationRequest.ConnectorStatus.OCCUPIED)
                    .findAny();

            assertThat(statusNotificationCallOptional).isPresent();
        });

        await().untilAsserted(() -> {
            Optional<Call> transactionEventCallOptional = server.getReceivedCalls(STATION_ID).stream().filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT).findAny();

            assertThat(transactionEventCallOptional).isPresent();

            TransactionEventRequest transactionEventPayload = (TransactionEventRequest) transactionEventCallOptional.get().getPayload();
            assertThat(transactionEventPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.STARTED);
            assertThat(transactionEventPayload.getSeqNo()).isEqualTo(0);

            assertThat(transactionEventPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionEventPayload.getEvse().getId()).isEqualTo(DEFAULT_EVSE_ID);
        });
    }

    @Test
    void shouldStartChargingWithPreAuthorization() {
        //given
        String tokenId = DEFAULT_TOKEN_ID;

        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(tokenId);
        mockSuccessfulTransactionEventAnswer();

        stationSimulatorRunner.run();

        triggerUserAction(STATION_ID, new Authorize(tokenId, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> stationSimulatorRunner.getStation(STATION_ID).getState().hasAuthorizedToken());

        //when
        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        //then
        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.STARTED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();

            TransactionEventRequest transactionStartedPayload = (TransactionEventRequest) transactionStartedEventOptional.get().getPayload();
            assertThat(transactionStartedPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.STARTED);
            assertThat(transactionStartedPayload.getSeqNo()).isEqualTo(0);
            assertThat(transactionStartedPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(DEFAULT_EVSE_ID);
            assertThat(transactionStartedPayload.getIdToken().getIdToken().toString()).isEqualTo(tokenId);
            assertThat(transactionStartedPayload.getTransactionData().getChargingState()).isNull();
        });

        await().untilAsserted(() -> {
            Optional<Call> transactionUpdatedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getTransactionData().getChargingState() == TransactionData.ChargingState.EV_DETECTED)
                    .findAny();

            assertThat(transactionUpdatedEventOptional).isPresent();

            TransactionEventRequest transactionStartedPayload = (TransactionEventRequest) transactionUpdatedEventOptional.get().getPayload();
            assertThat(transactionStartedPayload.getTriggerReason()).isEqualTo(TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN);
            assertThat(transactionStartedPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.UPDATED);
            assertThat(transactionStartedPayload.getSeqNo()).isEqualTo(DEFAULT_SEQ_NUMBER);
            assertThat(transactionStartedPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(DEFAULT_EVSE_ID);
        });

        await().untilAsserted(() -> {
            Optional<Call> transactionUpdatedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getTransactionData().getChargingState() == TransactionData.ChargingState.CHARGING)
                    .findAny();

            assertThat(transactionUpdatedEventOptional).isPresent();

            TransactionEventRequest transactionStartedPayload = (TransactionEventRequest) transactionUpdatedEventOptional.get().getPayload();
            assertThat(transactionStartedPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.UPDATED);
            assertThat(transactionStartedPayload.getTriggerReason()).isEqualTo(TransactionEventRequest.TriggerReason.CHARGING_STATE_CHANGED);
            assertThat(transactionStartedPayload.getSeqNo()).isEqualTo(2);
            assertThat(transactionStartedPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(DEFAULT_EVSE_ID);
        });

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(DEFAULT_EVSE_ID)).isTrue());
    }

    @Test
    void shouldStartChargingWithPostAuthorization() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(DEFAULT_TOKEN_ID);
        mockSuccessfulTransactionEventAnswer();

        stationSimulatorRunner.run();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        //when
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        //then
        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.STARTED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();

            TransactionEventRequest transactionStartedPayload = (TransactionEventRequest) transactionStartedEventOptional.get().getPayload();
            assertThat(transactionStartedPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.STARTED);
            assertThat(transactionStartedPayload.getSeqNo()).isEqualTo(0);
            assertThat(transactionStartedPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(DEFAULT_EVSE_ID);
            assertThat(transactionStartedPayload.getTransactionData().getChargingState()).isEqualTo(TransactionData.ChargingState.EV_DETECTED);
        });

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();

            TransactionEventRequest transactionStartedPayload = (TransactionEventRequest) transactionStartedEventOptional.get().getPayload();
            assertThat(transactionStartedPayload.getEventType()).isEqualTo(TransactionEventRequest.EventType.UPDATED);
            assertThat(transactionStartedPayload.getSeqNo()).isEqualTo(DEFAULT_SEQ_NUMBER);
            assertThat(transactionStartedPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(DEFAULT_EVSE_ID);
            assertThat(transactionStartedPayload.getTransactionData().getChargingState()).isEqualTo(TransactionData.ChargingState.CHARGING);
        });

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(DEFAULT_EVSE_ID)).isTrue());
    }


    @Test
    void shouldStopChargingOnSecondAuth() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(DEFAULT_TOKEN_ID);
        mockSuccessfulTransactionEventAnswer();

        stationSimulatorRunner.run();
        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.STARTED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isTrue());

        //when
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        //then
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isFalse());

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });
    }

    @Test
    void shouldEndOngoingTransactionOnSecondAuth() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(DEFAULT_TOKEN_ID);
        mockSuccessfulTransactionEventAnswer();

        stationSimulatorRunner.run();
        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.STARTED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(DEFAULT_EVSE_ID)).isTrue());

        //when
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        //then
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(DEFAULT_EVSE_ID)).isFalse());

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        triggerUserAction(STATION_ID, new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.ENDED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().findEvse(DEFAULT_EVSE_ID).hasTokenId()).isFalse();
        assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().hasOngoingTransaction(DEFAULT_EVSE_ID)).isFalse();
    }

    @Test
    void shouldPreserveTransactionIdPerEvseForWholeSession() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(DEFAULT_TOKEN_ID);
        mockSuccessfulTransactionEventAnswer();

        stationSimulatorRunner.run();
        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        //when
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(DEFAULT_EVSE_ID)).isTrue());
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(DEFAULT_EVSE_ID)).isFalse());
        triggerUserAction(STATION_ID, new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        //then
        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.STARTED)
                    .findAny();

            Optional<Call> transactionEndedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.ENDED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
            assertThat(transactionEndedEventOptional).isPresent();

            TransactionEventRequest transactionStartedPayload = fromJson(toJson(transactionStartedEventOptional.get().getPayload()), TransactionEventRequest.class);
            TransactionEventRequest transactionEndedPayload = fromJson(toJson(transactionEndedEventOptional.get().getPayload()), TransactionEventRequest.class);

            List<String> transactionIds = Stream.of(transactionStartedPayload, transactionEndedPayload)
                    .map(trn -> trn.getTransactionData().getId().toString())
                    .collect(toList());

            assertThat(transactionIds).containsOnly("1");

            List<Long> sequenceNumbers = Stream.of(transactionStartedPayload, transactionEndedPayload)
                    .map(TransactionEventRequest::getSeqNo).collect(toList());

            assertThat(new HashSet<>(sequenceNumbers)).hasSize(2);
        });
    }

    @Test
    void shouldImmediatelyRebootWithOngoingTransaction() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(DEFAULT_TOKEN_ID);
        mockSuccessfulTransactionEventAnswer();

        stationSimulatorRunner.run();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isTrue());

        String immediateResetRequest = new Call(UUID.randomUUID().toString(), ActionType.RESET, new ResetRequest().withType(ResetRequest.Type.IMMEDIATE))
                .toJson();

        //when
        stationSimulatorRunner.getStation(STATION_ID).sendMessage(new StationMessage(STATION_ID, StationMessage.Type.OCPP_MESSAGE, immediateResetRequest));

        //then
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isFalse());
        await().untilAsserted(() -> {
            Optional<Call> bootNotificationOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.BOOT_NOTIFICATION)
                    .filter(call -> ((BootNotificationRequest) call.getPayload()).getReason() == BootNotificationRequest.Reason.REMOTE_RESET)
                    .findAny();

            assertThat(bootNotificationOptional).isPresent();
        });

        await().untilAsserted(() -> {
            Optional<Call> statusNotificationOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.STATUS_NOTIFICATION)
                    .filter(call -> ((StatusNotificationRequest) call.getPayload()).getConnectorStatus() == StatusNotificationRequest.ConnectorStatus.OCCUPIED)
                    .findAny();

            assertThat(statusNotificationOptional).isPresent();
        });
    }

    private void mockSuccessfulBootNotificationAnswer() {
        mockSuccessfulBootNotificationAnswer(ZonedDateTime.now(), 100);
    }

    private void mockSuccessfulBootNotificationAnswer(ZonedDateTime serverTime, int heartBeatIntervalSec) {
        server.addCallAnswer(call -> call.getActionType() == ActionType.BOOT_NOTIFICATION,
                call -> "[3, \"" + call.getMessageId() + "\", {\"currentTime\":\"" + serverTime + "\", \"interval\":\"" + heartBeatIntervalSec + "\", \"status\":\"Accepted\"}]");
    }

    private void mockSuccessfulStatusNotificationAnswer() {
        server.addCallAnswer(
                call -> call.getActionType() == ActionType.STATUS_NOTIFICATION,
                call -> "[3, \"" + call.getMessageId() + "\", {}]");
    }

    private void mockSuccessfulAuthorizationAnswer(String tokenId) {
        server.addCallAnswer(call -> {
            if (call.getActionType() != ActionType.AUTHORIZE) {
                return false;
            }
            IdToken token = ((AuthorizeRequest) call.getPayload()).getIdToken();
            return token.getIdToken().toString().equals(tokenId) && token.getType() == IdToken.Type.ISO_14443;
        }, call -> {
            AuthorizeResponse response = new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED));
            String payload = toJson(response);
            return "[3, \"" + call.getMessageId() + "\", " + payload + "]";
        });
    }

    private void mockSuccessfulTransactionEventAnswer() {
        server.addCallAnswer(
                call -> call.getActionType() == ActionType.TRANSACTION_EVENT,
                call -> "[3, \"" + call.getMessageId() + "\", {}]");
    }

    private void mockSuccessfulGetBaseReportAnswer() {
        server.addCallAnswer(
                call -> call.getActionType() == ActionType.GET_BASE_REPORT,
                call -> "[3, \"" + call.getMessageId() + "\", {\"status\":\"Accepted\"}]");
    }

    @SneakyThrows
    private String toJson(Object object) {
        return ObjectMapperHolder.getJsonObjectMapper().writeValueAsString(object);
    }

    @SneakyThrows
    private <T> T fromJson(String json, Class<T> clz) {
        return ObjectMapperHolder.getJsonObjectMapper().readValue(json, clz);
    }

    private void triggerUserAction(String stationId, UserMessage action) {
        stationSimulatorRunner.getStation(stationId).sendMessage(new StationMessage(stationId, StationMessage.Type.USER_ACTION, action));
    }

    static class NotifyReportComparator implements Comparator<NotifyReportRequest>
    {
        public int compare(NotifyReportRequest first, NotifyReportRequest second)
        {
            return first.getSeqNo().compareTo(second.getSeqNo());
        }
    }
}