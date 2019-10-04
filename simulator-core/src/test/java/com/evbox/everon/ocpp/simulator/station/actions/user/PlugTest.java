package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.states.WaitingForPlugState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationResponse;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlugTest {

    @Mock
    StationPersistenceLayer stationPersistenceLayerMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    StationStateFlowManager stationStateFlowManagerMock;
    @Mock
    Evse evseMock;
    @Mock
    Connector connectorMock;

    Plug plug;

    @BeforeEach
    void setUp() {
        this.plug = new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        this.stationStateFlowManagerMock = new StationStateFlowManager(null, stationPersistenceLayerMock, stationMessageSenderMock);
        this.stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new AvailableState());
    }

    @Test
    void shouldThrowExceptionOnIllegalState() {

        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.LOCKED);

        assertThrows(IllegalStateException.class, () -> plug.perform(stationStateFlowManagerMock));

    }

    @Test
    void verifyTransactionEventUpdate() {

        // given
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.hasOngoingTransaction()).thenReturn(true);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);

        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new WaitingForPlugState());

        // when
        plug.perform(stationStateFlowManagerMock);

        // then
        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(2)).sendTransactionEventUpdate(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                isNull(), any(TransactionData.ChargingState.class));

    }

    @Test
    void verifyTransactionEventStart() {

        // given
        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationPersistenceLayerMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);

        // when
        plug.perform(stationStateFlowManagerMock);

        // then
        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventStart(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                any(TransactionData.ChargingState.class));

    }
}
