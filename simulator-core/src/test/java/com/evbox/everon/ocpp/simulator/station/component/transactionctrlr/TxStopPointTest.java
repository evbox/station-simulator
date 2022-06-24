package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.states.ChargingState;
import com.evbox.everon.ocpp.simulator.station.evse.states.StoppedState;
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
import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.UNAVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.NONE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.STOPPED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TxStopPointTest {


    private Connector connector;

    private Evse evse;

    private StationStore stationStore;

    @Mock
    StationMessageSender stationMessageSenderMock;

    private StateManager stateManager;

    @Captor
    ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> statusNotificationCaptor;

    @Captor
    ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> authorizeCaptor;

    @Captor
    ArgumentCaptor<Subscriber<TransactionEventRequest, TransactionEventResponse>> transactionEventCaptor;


    @BeforeEach
    void setUp() {
        connector = new Connector(1, UNPLUGGED, AVAILABLE);
        evse = new Evse(DEFAULT_EVSE_ID, UNAVAILABLE, new EvseTransaction(EvseTransactionStatus.NONE), List.of(connector));
        stationStore = new StationStore(Clock.systemUTC(), 10, 100,
                Map.of(DEFAULT_EVSE_ID, evse));
        evse.setEvseState(new ChargingState());
        stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
    }

    @Test
    void verifyStopOnlyOnAuthorizedAuthAction() {
        connector.plug();
        connector.lock();
        evse.setToken(DEFAULT_TOKEN_ID);
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.STOP_AUTHORIZED, Reason.DE_AUTHORIZED, 0);
        assertEquals(STOPPED, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnlyOnAuthorizedUnplugAction() {
        evse.setEvseState(new StoppedState());
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, never()).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_DEPARTED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(NONE, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnlyOnEVConnectedUnplugAction() {
        evse.setEvseState(new StoppedState());
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_DEPARTED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(STOPPED, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnlyOnEVConnectedAuthAction() {
        connector.plug();
        connector.lock();
        evse.setToken(DEFAULT_TOKEN_ID);
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)));

        verify(stationMessageSenderMock, never()).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.STOP_AUTHORIZED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(NONE, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnlyOnPowerPathClosedUnplugAction() {
        evse.setEvseState(new StoppedState());
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_DEPARTED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(STOPPED, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnlyOnPowerPathClosedAuthAction() {
        evse.setToken((DEFAULT_TOKEN_ID));
        connector.plug();
        connector.lock();
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED )));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)));

        verify(stationMessageSenderMock, never()).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.STOP_AUTHORIZED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(NONE, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnlyOnPowerPathClosedAuthUnplugAction() {
        evse.setToken(DEFAULT_TOKEN_ID);
        connector.plug();
        connector.lock();
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)));

        verify(stationMessageSenderMock, never()).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.STOP_AUTHORIZED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(NONE, evse.getTransaction().getStatus());
        assertEquals(StoppedState.NAME, evse.getEvseState().getStateName());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_DEPARTED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(STOPPED, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedAuthAction() {
        connector.plug();
        connector.lock();
        evse.setToken(DEFAULT_TOKEN_ID);
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.STOP_AUTHORIZED, Reason.DE_AUTHORIZED, 0);
        assertEquals(STOPPED, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedUnplugAction() {
        evse.setEvseState(new StoppedState());
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, never()).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_DEPARTED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(NONE, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStartOnAuthorizedAndPowerPathClosedAuthUnplugAction() {
        connector.plug();
        connector.lock();
        evse.setToken(DEFAULT_TOKEN_ID);
        stationStore.setTxStopPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)));

        verify(stationMessageSenderMock, never()).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.STOP_AUTHORIZED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(NONE, evse.getTransaction().getStatus());
        assertEquals(StoppedState.NAME, evse.getEvseState().getStateName());

        evse.setEvseState(new StoppedState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_DEPARTED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(STOPPED, evse.getTransaction().getStatus());

    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedAndPowerPathClosedUnplugAction() {
        evse.setEvseState(new StoppedState());
        stationStore.setTxStopPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_DEPARTED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(STOPPED, evse.getTransaction().getStatus());
    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedAndPowerPathClosedAuthAction() {
        connector.plug();
        connector.lock();
        evse.setToken(DEFAULT_TOKEN_ID);
        stationStore.setTxStopPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)));

        verify(stationMessageSenderMock, never()).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.STOP_AUTHORIZED, Reason.EV_DISCONNECTED, 0L);
        assertEquals(NONE, evse.getTransaction().getStatus());
    }

}
