package com.evbox.everon.ocpp.simulator.station.actions.system;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.EvseStateManager;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.states.*;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CancelRemoteStartTransactionTest {

    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    EvseStateManager evseStateManagerMock;

    private CancelRemoteStartTransaction cancelRemoteStartTransaction;

    @BeforeEach
    void setUp() {
        this.cancelRemoteStartTransaction = new CancelRemoteStartTransaction(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        this.evseStateManagerMock = new EvseStateManager(null, stationStoreMock, stationMessageSenderMock);
        this.evseStateManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new AvailableState());
    }

    @Test
    void shouldReleaseConnector() {

        evseStateManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new WaitingForPlugState());

        Evse evse = mock(Evse.class, RETURNS_DEEP_STUBS);
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evse);

        cancelRemoteStartTransaction.perform(stationStoreMock, stationMessageSenderMock, evseStateManagerMock);

        verify(stationMessageSenderMock).sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, StatusNotificationRequest.ConnectorStatus.AVAILABLE);
        verify(evse).stopTransaction();
        verify(stationMessageSenderMock).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TransactionEventRequest.TriggerReason.EV_CONNECT_TIMEOUT, TransactionData.StoppedReason.TIMEOUT);

        assertThat(evseStateManagerMock.getStateForEvse(DEFAULT_EVSE_ID).getStateName()).isEqualTo(AvailableState.NAME);
    }

    @Test
    void verifyTransactionStatusNotification() {

        evseStateManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new ChargingState());

        cancelRemoteStartTransaction.perform(stationStoreMock, stationMessageSenderMock, evseStateManagerMock);
        verify(stationMessageSenderMock, times(0)).sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, StatusNotificationRequest.ConnectorStatus.AVAILABLE);

    }

}