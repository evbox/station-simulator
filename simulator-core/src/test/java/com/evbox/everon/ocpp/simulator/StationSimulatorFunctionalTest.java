package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.message.ObjectMapperHolder;
import com.evbox.everon.ocpp.simulator.station.StationInboxMessage;
import com.evbox.everon.ocpp.simulator.user.interaction.UserAction;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.evbox.everon.ocpp.v20.message.station.*;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.simulator.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.simulator.SimulatorConfigCreator.createStationConfiguration;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class StationSimulatorFunctionalTest {

    private static final String TOKEN_ID = "045918E24B4D80";
    private static final String STATION_ID = "EVB-P17390866";

    private WebSocketServerMock server = new WebSocketServerMock("/ocpp", 0);

    private StationSimulator stationSimulator;

    private String ocppServerUrl;

    @Before
    public void setUp() {
        Awaitility.setDefaultTimeout(5, TimeUnit.SECONDS);
        server.start();
        ocppServerUrl = "ws://localhost:" + server.getPort() + "/ocpp";

        SimulatorConfiguration.Station stationConfiguration = createStationConfiguration(STATION_ID, 1, 1);
        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(stationConfiguration);

        stationSimulator = new StationSimulator(ocppServerUrl, simulatorConfiguration);
        mockSuccessfulStatusNotificationAnswer();
    }

    @Test
    public void shouldStartStation() {
        //when
        mockSuccessfulBootNotificationAnswer();
        stationSimulator.start();

        //then
        await().untilAsserted(() -> {
            List<Call> stationCalls = server.getReceivedCalls(STATION_ID);

            assertThat(stationCalls).hasSize(2);
            assertThat(stationCalls.stream().anyMatch(call -> call.getActionType() == ActionType.BOOT_NOTIFICATION)).isTrue();
            Optional<Call> statusNotificationOptional = stationCalls.stream().filter(call -> call.getActionType() == ActionType.STATUS_NOTIFICATION).findAny();

            assertThat(statusNotificationOptional).isPresent();
            StatusNotificationRequest payload = (StatusNotificationRequest) statusNotificationOptional.get().getPayload();
            assertThat(payload.getEvseId()).isEqualTo(1);
            assertThat(payload.getConnectorId()).isEqualTo(1);
            assertThat(payload.getConnectorStatus()).isEqualTo(StatusNotificationRequest.ConnectorStatus.AVAILABLE);
        });
    }

    @Test
    public void shouldStartMultipleStations() {
        //given
        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(
                createStationConfiguration(STATION_ID, 1, 1),
                createStationConfiguration("EVB-P18090564", 1, 2));
        stationSimulator = new StationSimulator(ocppServerUrl, simulatorConfiguration);

        mockSuccessfulBootNotificationAnswer();

        //when
        stationSimulator.start();

        //then
        await().untilAsserted(() -> {
            List<Call> stationCalls1 = server.getReceivedCalls(STATION_ID);
            List<Call> stationCalls2 = server.getReceivedCalls("EVB-P18090564");

            assertThat(server.getReceivedCalls()).hasSize(5);

            assertThat(stationCalls1.stream().anyMatch(call -> call.getActionType() == ActionType.BOOT_NOTIFICATION)).isTrue();
            assertThat(stationCalls2.stream().anyMatch(call -> call.getActionType() == ActionType.BOOT_NOTIFICATION)).isTrue();

            assertThat(stationCalls1.stream().filter(call -> call.getActionType() == ActionType.STATUS_NOTIFICATION).anyMatch(call -> {
                StatusNotificationRequest payload = (StatusNotificationRequest) call.getPayload();
                return payload.getEvseId().equals(1) &&
                        payload.getConnectorId().equals(1) &&
                        payload.getConnectorStatus() == StatusNotificationRequest.ConnectorStatus.AVAILABLE;
            })).isTrue();


            assertThat(stationCalls2.stream().filter(call -> call.getActionType() == ActionType.STATUS_NOTIFICATION).anyMatch(call -> {
                StatusNotificationRequest payload = (StatusNotificationRequest) call.getPayload();
                return payload.getEvseId().equals(1) &&
                        payload.getConnectorId().equals(1) &&
                        payload.getConnectorStatus() == StatusNotificationRequest.ConnectorStatus.AVAILABLE;
            })).isTrue();

            assertThat(stationCalls2.stream().filter(call -> call.getActionType() == ActionType.STATUS_NOTIFICATION).anyMatch(call -> {
                StatusNotificationRequest payload = (StatusNotificationRequest) call.getPayload();
                return payload.getEvseId().equals(1) &&
                        payload.getConnectorId().equals(2) &&
                        payload.getConnectorStatus() == StatusNotificationRequest.ConnectorStatus.AVAILABLE;
            })).isTrue();
        });
    }

    @Test
    public void shouldSendHeartbeatsWithGivenInterval() {
        //given
        int intervalInSec = 1;

        server.addCallAnswer(
                call -> call.getActionType() == ActionType.BOOT_NOTIFICATION,
                call -> "[3, \"" + call.getMessageId() + "\", {\"currentTime\":\"" + ZonedDateTime.now() + "\", \"interval\":" + intervalInSec + ", \"status\":\"Accepted\"}]");

        //when
        stationSimulator.start();

        //then
        await().atMost(2000, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            long heartbeatCallsSent = stationSimulator.getStation(STATION_ID).getSentCalls().values()
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.HEARTBEAT)
                    .count();

            assertThat(heartbeatCallsSent).isGreaterThanOrEqualTo(1).isLessThan(3);
        });
    }

    @Test
    public void shouldAdjustCurrentTimeBasedOnHeartbeatResponse() {
        //given
        ZonedDateTime serverTime = ZonedDateTime.of(2035, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC);

        mockSuccessfulBootNotificationAnswer(ZonedDateTime.now(), 1);

        server.addCallAnswer(
                call -> call.getActionType() == ActionType.HEARTBEAT,
                call -> "[3, \"" + call.getMessageId() + "\", {\"currentTime\":\"" + serverTime + "\", \"status\":\"Accepted\"}]");

        //when
        stationSimulator.start();

        //then
        await().untilAsserted(() -> {
            Instant timeOfStation = stationSimulator.getStation(STATION_ID).getState().getCurrentTime();
            assertThat(timeOfStation).isAfterOrEqualTo(serverTime.toInstant());
        });
    }

    @Test
    public void shouldSendConnectorStatusAndTransactionStartedWhenCablePluggedIn() {
        //given
        mockSuccessfulBootNotificationAnswer();
        stationSimulator.start();

        //when
        triggerUserAction(STATION_ID, new UserAction.Plug(1));

        //then
        await().untilAsserted(() -> {

            List<Call> stationCalls = server.getReceivedCalls(STATION_ID);

            Optional<Call> statusNotificationCallOptional = stationCalls.stream()
                    .filter(call -> call.getActionType() == ActionType.STATUS_NOTIFICATION)
                    .filter(call -> ((StatusNotificationRequest)call.getPayload()).getConnectorStatus() == StatusNotificationRequest.ConnectorStatus.OCCUPIED)
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
            assertThat(transactionEventPayload.getEvse().getId()).isEqualTo(1);
        });
    }

    @Test
    public void shouldStartChargingWithPreAuthorization() {
        //given
        String tokenId = "045918E24B4D80";

        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(tokenId);
        mockSuccessfulTransactionEventAnswer();

        stationSimulator.start();

        triggerUserAction(STATION_ID, new UserAction.Authorize(tokenId, 1));

        await().untilAsserted(() -> stationSimulator.getStation(STATION_ID).getState().hasAuthorizedToken());

        //when
        triggerUserAction(STATION_ID, new UserAction.Plug(1));

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
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(1);
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
            assertThat(transactionStartedPayload.getSeqNo()).isEqualTo(1);
            assertThat(transactionStartedPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(1);
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
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(1);
        });

        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isTrue());
    }

    @Test
    public void shouldStartChargingWithPostAuthorization() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer("045918E24B4D80");
        mockSuccessfulTransactionEventAnswer();

        stationSimulator.start();

        triggerUserAction(STATION_ID, new UserAction.Plug(1));

        //when
        triggerUserAction(STATION_ID, new UserAction.Authorize("045918E24B4D80", 1));

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
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(1);
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
            assertThat(transactionStartedPayload.getSeqNo()).isEqualTo(1);
            assertThat(transactionStartedPayload.getTransactionData().getId().toString()).isEqualTo("1");
            assertThat(transactionStartedPayload.getEvse().getId()).isEqualTo(1);
            assertThat(transactionStartedPayload.getTransactionData().getChargingState()).isEqualTo(TransactionData.ChargingState.CHARGING);
        });

        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isTrue());
    }


    @Test
    public void shouldStopChargingOnSecondAuth() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer("045918E24B4D80");
        mockSuccessfulTransactionEventAnswer();

        stationSimulator.start();
        triggerUserAction(STATION_ID, new UserAction.Plug(1));
        triggerUserAction(STATION_ID, new UserAction.Authorize("045918E24B4D80", 1));

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

        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isTrue());

        //when
        triggerUserAction(STATION_ID, new UserAction.Authorize("045918E24B4D80", 1));

        //then
        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isFalse());

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
    public void shouldEndOngoingTransactionOnSecondAuth() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer("045918E24B4D80");
        mockSuccessfulTransactionEventAnswer();

        stationSimulator.start();
        triggerUserAction(STATION_ID, new UserAction.Plug(1));
        triggerUserAction(STATION_ID, new UserAction.Authorize("045918E24B4D80", 1));

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

        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isTrue());

        //when
        triggerUserAction(STATION_ID, new UserAction.Authorize("045918E24B4D80", 1));

        //then
        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isFalse());

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.UPDATED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        triggerUserAction(STATION_ID, new UserAction.Unplug(1));

        await().untilAsserted(() -> {
            Optional<Call> transactionStartedEventOptional = server.getReceivedCalls(STATION_ID)
                    .stream()
                    .filter(call -> call.getActionType() == ActionType.TRANSACTION_EVENT)
                    .filter(call -> ((TransactionEventRequest) call.getPayload()).getEventType() == TransactionEventRequest.EventType.ENDED)
                    .findAny();

            assertThat(transactionStartedEventOptional).isPresent();
        });

        assertThat(stationSimulator.getStation(STATION_ID).getState().hasAuthorizedToken(1)).isFalse();
        assertThat(stationSimulator.getStation(STATION_ID).getState().hasOngoingTransaction(1)).isFalse();
    }

    @Test
    public void shouldPreserveTransactionIdPerEvseForWholeSession() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(TOKEN_ID);
        mockSuccessfulTransactionEventAnswer();

        stationSimulator.start();
        triggerUserAction(STATION_ID, new UserAction.Plug(1));

        //when
        triggerUserAction(STATION_ID, new UserAction.Authorize(TOKEN_ID, 1));
        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isTrue());
        triggerUserAction(STATION_ID, new UserAction.Authorize(TOKEN_ID, 1));
        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isFalse());
        triggerUserAction(STATION_ID, new UserAction.Unplug(1));

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
    public void shouldReplyToSetVariablesRequest() {
        //given
        mockSuccessfulBootNotificationAnswer();
        stationSimulator.start();

        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withVariable(new Variable().withName(new CiString.CiString50("ReserveConnectorZeroSupported")))
                .withComponent(new Component().withName(new CiString.CiString50("ReservationFeature")))
                .withAttributeValue(new CiString.CiString1000("true")).withAttributeType(SetVariableDatum.AttributeType.TARGET);

        SetVariablesRequest request = new SetVariablesRequest().withSetVariableData(singletonList(setVariableDatum));

        Call call = new Call(UUID.randomUUID().toString(), ActionType.SET_VARIABLES, request);

        //when
        stationSimulator.getStation(STATION_ID).getInbox().add(new StationInboxMessage(StationInboxMessage.Type.OCPP_MESSAGE, call.toJson()));

        //then
        await().untilAsserted(() -> {
            Optional<CallResult> responseFromStationOptional = server.getReceivedCallResults(STATION_ID).stream()
                    .findAny();

            assertThat(responseFromStationOptional).isPresent();
        });
    }

    @Test
    public void shouldImmediatelyRebootWithOngoingTransaction() {
        //given
        mockSuccessfulBootNotificationAnswer();
        mockSuccessfulAuthorizationAnswer(TOKEN_ID);
        mockSuccessfulTransactionEventAnswer();

        stationSimulator.start();

        triggerUserAction(STATION_ID, new UserAction.Plug(1));
        triggerUserAction(STATION_ID, new UserAction.Authorize(TOKEN_ID, 1));
        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isTrue());

        String immediateResetRequest = new Call(UUID.randomUUID().toString(), ActionType.RESET, new ResetRequest().withType(ResetRequest.Type.IMMEDIATE))
                .toJson();
        //when
        stationSimulator.getStation(STATION_ID).getInbox().add(new StationInboxMessage(StationInboxMessage.Type.OCPP_MESSAGE, immediateResetRequest));

        //then
        await().untilAsserted(() -> assertThat(stationSimulator.getStation(STATION_ID).getState().isCharging(1)).isFalse());
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

    @SneakyThrows
    private String toJson(Object object) {
        return ObjectMapperHolder.getJsonObjectMapper().writeValueAsString(object);
    }

    @SneakyThrows
    private <T> T fromJson(String json, Class<T> clz) {
        return ObjectMapperHolder.getJsonObjectMapper().readValue(json, clz);
    }

    private void triggerUserAction(String stationId, UserAction action) {
        stationSimulator.getStation(stationId).getInbox().add(new StationInboxMessage(StationInboxMessage.Type.USER_ACTION, action));
    }
}