package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForPlugState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v201.message.station.ChargingState;
import com.evbox.everon.ocpp.v201.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v201.message.station.StatusNotificationResponse;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlugTest {

    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    StateManager stateManagerMock;
    @Mock
    Evse evseMock;
    @Mock
    Connector connectorMock;

    Plug plug;

    @BeforeEach
    void setUp() {
        this.plug = new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        this.stateManagerMock = new StateManager(null, stationStoreMock, stationMessageSenderMock);
        EvseTransaction transaction = new EvseTransaction(UUID.randomUUID().toString());
        lenient().when(evseMock.createTransaction(anyString())).thenReturn(transaction);
        lenient().when(evseMock.getTransaction()).thenReturn(transaction);
        when(evseMock.getEvseState()).thenReturn(new AvailableState());
    }

    @Test
    void shouldFutureFailOnIllegalState() throws Exception {

        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.LOCKED);

        assertEquals(UserMessageResult.FAILED, plug.perform(stateManagerMock).get());

    }

    @Test
    void verifyTransactionEventUpdate() {

        // given
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.hasOngoingTransaction()).thenReturn(true);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);

        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        // when
        plug.perform(stateManagerMock);

        // then
        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(2)).sendTransactionEventUpdate(anyInt(), anyInt(), any(TriggerReason.class),
                isNull(), any(ChargingState.class));

    }

    @Test
    void verifyTransactionEventStart() {

        // given
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);

        // when
        plug.perform(stateManagerMock);

        // then
        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventStart(anyInt(), anyInt(), any(TriggerReason.class),
                any(ChargingState.class));

    }
}
