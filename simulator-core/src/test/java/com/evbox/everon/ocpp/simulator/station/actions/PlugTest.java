package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
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

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlugTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    Evse evseMock;
    @Mock
    Connector connectorMock;

    Plug plug;

    @BeforeEach
    void setUp() {
        this.plug = new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
    }

    @Test
    void shouldThrowExceptionOnIllegalState() {

        when(stationStateMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.LOCKED);

        assertThrows(IllegalStateException.class, () -> plug.perform(stationStateMock, stationMessageSenderMock));

    }

    @Test
    void verifyTransactionEventUpdate() {

        // given
        when(stationStateMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.hasOngoingTransaction()).thenReturn(true);
        when(evseMock.hasTokenId()).thenReturn(true);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);

        // when
        plug.perform(stationStateMock, stationMessageSenderMock);

        // then
        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventUpdateAndSubscribe(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                isNull(), any(TransactionData.ChargingState.class), any(Subscriber.class));

    }

    @Test
    void verifyTransactionEventStart() {

        // given
        when(stationStateMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(evseMock.hasOngoingTransaction()).thenReturn(true);
        when(evseMock.hasTokenId()).thenReturn(false);
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);

        // when
        plug.perform(stationStateMock, stationMessageSenderMock);

        // then
        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventStart(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                any(TransactionData.ChargingState.class));

    }
}
