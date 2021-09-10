package com.evbox.everon.ocpp.simulator.station.actions.system;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.ChargingState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForPlugState;
import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelRemoteStartTransactionTest {

    @Mock
    Evse evseMock;
    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    StateManager stateManagerMock;

    private CancelRemoteStartTransaction cancelRemoteStartTransaction;

    @BeforeEach
    void setUp() {
        this.cancelRemoteStartTransaction = new CancelRemoteStartTransaction(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        this.stateManagerMock = new StateManager(null, stationStoreMock, stationMessageSenderMock);
        when(evseMock.getEvseState()).thenReturn(new AvailableState());
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
    }

    @Test
    void shouldReleaseConnector() {

        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        cancelRemoteStartTransaction.perform(stationStoreMock, stationMessageSenderMock, stateManagerMock);

        verify(stationMessageSenderMock).sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, ConnectorStatus.AVAILABLE);
        verify(evseMock).stopTransaction();
        verify(stationMessageSenderMock).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_CONNECT_TIMEOUT, Reason.TIMEOUT, 0L);
        verify(evseMock).setEvseState(argThat(s -> s.getStateName().equals(AvailableState.NAME)));
    }

    @Test
    void verifyTransactionStatusNotification() {

        when(evseMock.getEvseState()).thenReturn(new ChargingState());

        cancelRemoteStartTransaction.perform(stationStoreMock, stationMessageSenderMock, stateManagerMock);
        verify(stationMessageSenderMock, times(0)).sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, ConnectorStatus.AVAILABLE);

    }

}
