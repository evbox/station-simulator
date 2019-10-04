package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.EvseStateManager;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.states.WaitingForAuthorizationState;
import com.evbox.everon.ocpp.simulator.station.states.WaitingForPlugState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TxStartPointTest {

    @Mock
    Connector connectorMock;

    @Mock
    Evse evseMock;

    @Mock
    StationStore stationStoreMock;

    @Mock
    StationMessageSender stationMessageSenderMock;

    @Mock
    EvseStateManager evseStateManagerMock;

    @Captor
    ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> statusNotificationCaptor;

    @Captor
    ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> authorizeCaptor;

    @BeforeEach
    void setUp() {
        this.evseStateManagerMock = new EvseStateManager(null, stationStoreMock, stationMessageSenderMock);
        evseStateManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new AvailableState());
    }

    @Test
    void verifyStartOnlyOnAuthorizedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnAuthorizedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnEVConnectedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnEVConnectedAuthAction() {
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedAuthAction() {
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedAuthPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());

        assertThat(evseStateManagerMock.getStateForEvse(DEFAULT_EVSE_ID).getStateName()).isEqualTo(WaitingForPlugState.NAME);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedPlugAuthAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(0)).createTransaction(anyString());

        assertThat(evseStateManagerMock.getStateForEvse(DEFAULT_EVSE_ID).getStateName()).isEqualTo(WaitingForAuthorizationState.NAME);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnAuthorizedAndPowerPathClosedAuthPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());

        assertThat(evseStateManagerMock.getStateForEvse(DEFAULT_EVSE_ID).getStateName()).isEqualTo(WaitingForPlugState.NAME);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnEVConnectedAndPowerPathClosedPlugAuthAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(0)).createTransaction(anyString());

        assertThat(evseStateManagerMock.getStateForEvse(DEFAULT_EVSE_ID).getStateName()).isEqualTo(WaitingForAuthorizationState.NAME);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAndPowerPathClosedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAndPowerPathClosedAuthAction() {
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        evseStateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TransactionEventRequest.TriggerReason.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

}
