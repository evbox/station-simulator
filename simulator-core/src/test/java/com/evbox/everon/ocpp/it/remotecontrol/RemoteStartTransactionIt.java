package com.evbox.everon.ocpp.it.remotecontrol;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import com.evbox.everon.ocpp.v201.message.station.IdToken;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.RequestStartTransactionRequest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v201.message.station.IdTokenType.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.waitAtMost;

public class RemoteStartTransactionIt extends StationSimulatorSetUp {

    private final int EV_CONNECTION_TIMEOUT = 2;

    @Test
    void shouldRemotelyStartTransactionRemoteStartFirst() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent.UPDATED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatus.AVAILABLE))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatus.OCCUPIED))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        IdToken token = new IdToken().withIdToken(new CiString.CiString36(DEFAULT_TOKEN_ID));
        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_START_TRANSACTION, new RequestStartTransactionRequest().withEvseId(DEFAULT_EVSE_ID).withIdToken(token).withRemoteStartId(1));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getTokenId()).isEqualTo(DEFAULT_TOKEN_ID));

    }

    @Test
    void shouldRemotelyStartTransactionPlugFirst() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent.UPDATED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatus.AVAILABLE))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatus.OCCUPIED))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        IdToken token = new IdToken().withIdToken(new CiString.CiString36(DEFAULT_TOKEN_ID));
        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_START_TRANSACTION, new RequestStartTransactionRequest().withEvseId(DEFAULT_EVSE_ID).withIdToken(token).withRemoteStartId(1));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getTokenId()).isEqualTo(DEFAULT_TOKEN_ID));

    }

    @Test
    void shouldTriggerTimeOut() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(com.evbox.everon.ocpp.v201.message.station.TransactionEvent.ENDED, Reason.TIMEOUT))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatus.AVAILABLE), times(2))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatus.OCCUPIED))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        IdToken token = new IdToken().withIdToken(new CiString.CiString36(DEFAULT_TOKEN_ID));
        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_START_TRANSACTION, new RequestStartTransactionRequest().withEvseId(DEFAULT_EVSE_ID).withIdToken(token).withRemoteStartId(1));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        waitAtMost(EV_CONNECTION_TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isFalse());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.AVAILABLE));

    }

    @Override
    protected int getEVConnectionTimeOutSec() {
        return EV_CONNECTION_TIMEOUT;
    }
}
