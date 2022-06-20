package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForAuthorizationState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForPlugState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v201.message.station.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.*;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.UNPLUGGED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TxStartPointTest {

    Connector connector;

    Evse evse;

    StationStore stationStore;

    @Mock
    StationMessageSender stationMessageSenderMock;

    StateManager stateManager;

    @Captor
    ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> statusNotificationCaptor;

    @Captor
    ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> authorizeCaptor;

    @BeforeEach
    void setUp() {
        connector = new Connector(1, UNPLUGGED, AVAILABLE);
        evse = new Evse(DEFAULT_EVSE_ID, List.of(connector));
        stationStore = new StationStore(Clock.systemUTC(), DEFAULT_HEARTBEAT_INTERVAL, 100,
                Map.of(DEFAULT_EVSE_ID, evse));
        this.stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
        evse.setEvseState(new AvailableState());
    }

    @Test
    void verifyStartOnlyOnAuthorizedPlugAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
    }

    @Test
    void verifyStartOnlyOnAuthorizedAuthAction() {
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
    }

    @Test
    void verifyStartOnlyOnEVConnectedPlugAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
    }

    @Test
    void verifyStartOnlyOnEVConnectedAuthAction() {
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedPlugAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        stationStore.setAuthorizeState(true);


        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedAuthAction() {
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedAuthPlugAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
        assertEquals(WaitingForPlugState.NAME, evse.getEvseState().getStateName());

        evse.setEvseState(new WaitingForPlugState());

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
        assertTrue(evse.isCharging());
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedPlugAuthAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
        assertEquals(WaitingForAuthorizationState.NAME, evse.getEvseState().getStateName());

        evse.setEvseState(new WaitingForAuthorizationState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
        assertTrue(evse.isCharging());
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAuthAction() {
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedPlugAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED)));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
    }

    @Test
    void verifyStartOnAuthorizedAndPowerPathClosedAuthPlugAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
        assertEquals(WaitingForPlugState.NAME, evse.getEvseState().getStateName());

        evse.setEvseState(new WaitingForPlugState());

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
        assertTrue(evse.isCharging());
    }

    @Test
    void verifyStartOnEVConnectedAndPowerPathClosedPlugAuthAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
        assertEquals(WaitingForAuthorizationState.NAME, evse.getEvseState().getStateName());

        evse.setEvseState(new WaitingForAuthorizationState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertNotNull(evse.getTransaction().getTransactionId());
        assertTrue(evse.isCharging());
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAndPowerPathClosedPlugAction() {
        connector.setCableStatus(UNPLUGGED);
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.CABLE_PLUGGED_IN, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAndPowerPathClosedAuthAction() {
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID, ChargingState.EV_CONNECTED);
        assertEquals(evse.getTransaction(), EvseTransaction.NONE);
    }

}
