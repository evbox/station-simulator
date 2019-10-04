package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.evse.ChargingStopReason;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.states.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TxStopPointTest {

    @Mock
    Connector connectorMock;

    @Mock
    Evse evseMock;

    @Mock
    StationPersistenceLayer stationPersistenceLayerMock;

    @Mock
    StationMessageSender stationMessageSenderMock;

    @Mock
    StationStateFlowManager stationStateFlowManagerMock;

    @Captor
    ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> statusNotificationCaptor;

    @Captor
    ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> authorizeCaptor;

    @Captor
    ArgumentCaptor<Subscriber<TransactionEventRequest, TransactionEventResponse>> transactionEventCaptor;


    @BeforeEach
    void setUp() {
        this.stationStateFlowManagerMock = new StationStateFlowManager(null, stationPersistenceLayerMock, stationMessageSenderMock);
        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new ChargingState());
    }

    @Test
    void verifyStopOnlyOnAuthorizedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evseMock.unlockConnector()).thenReturn(DEFAULT_CONNECTOR_ID);
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.STOP_AUTHORIZED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(1)).stopTransaction();
    }

    @Test
    void verifyStopOnlyOnAuthorizedUnplugAction() {
        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new StoppedState());

        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(0)).stopTransaction();
    }

    @Test
    void verifyStopOnlyOnEVConnectedUnplugAction() {
        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new StoppedState());

        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(1)).stopTransaction();
    }

    @Test
    void verifyStopOnlyOnEVConnectedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evseMock.unlockConnector()).thenReturn(DEFAULT_CONNECTOR_ID);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.STOP_AUTHORIZED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(0)).stopTransaction();
    }

    @Test
    void verifyStopOnlyOnPowerPathClosedUnplugAction() {
        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new StoppedState());

        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(1)).stopTransaction();
    }

    @Test
    void verifyStopOnlyOnPowerPathClosedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evseMock.unlockConnector()).thenReturn(DEFAULT_CONNECTOR_ID);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED )));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.STOP_AUTHORIZED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(0)).stopTransaction();
    }

    @Test
    void verifyStopOnlyOnPowerPathClosedAuthUnplugAction() {
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.STOP_AUTHORIZED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(0)).stopTransaction();

        assertThat(stationStateFlowManagerMock.getStateForEvse(DEFAULT_EVSE_ID).getStateName()).isEqualTo(StoppedState.NAME);

        stationStateFlowManagerMock.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(1)).stopTransaction();
    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evseMock.unlockConnector()).thenReturn(DEFAULT_CONNECTOR_ID);
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.STOP_AUTHORIZED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(1)).stopTransaction();
    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedUnplugAction() {
        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new StoppedState());

        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(0)).stopTransaction();
    }

    @Test
    void verifyStartOnAuthorizedAndPowerPathClosedAuthUnplugAction() {
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.STOP_AUTHORIZED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(0)).createTransaction(anyString());

        assertThat(stationStateFlowManagerMock.getStateForEvse(DEFAULT_EVSE_ID).getStateName()).isEqualTo(StoppedState.NAME);

        stationStateFlowManagerMock.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(1)).stopTransaction();
    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedAndPowerPathClosedUnplugAction() {
        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new StoppedState());

        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(1)).stopTransaction();
    }

    @Test
    void verifyStopOnAuthorizedAndEVConnectedAndPowerPathClosedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evseMock.unlockConnector()).thenReturn(DEFAULT_CONNECTOR_ID);
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        stationStateFlowManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED)).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.STOP_AUTHORIZED, TransactionData.StoppedReason.EV_DISCONNECTED);
        verify(evseMock, times(0)).stopTransaction();
    }

}