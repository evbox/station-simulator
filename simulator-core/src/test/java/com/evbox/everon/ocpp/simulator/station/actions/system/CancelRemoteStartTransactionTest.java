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

import java.time.Clock;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.STOPPED;
import static org.assertj.core.util.Lists.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelRemoteStartTransactionTest {
    Evse evse;

    StationStore stationStore;
    @Mock
    StationMessageSender stationMessageSenderMock;

    StateManager stateManager;

    private CancelRemoteStartTransaction cancelRemoteStartTransaction;

    @BeforeEach
    void setUp() {
        evse = new Evse(DEFAULT_EVSE_ID, emptyList());
        evse.setEvseState(new AvailableState());
        stationStore = new StationStore(Clock.systemUTC(), 10, 100, Map.of(DEFAULT_EVSE_ID, evse));
        this.cancelRemoteStartTransaction = new CancelRemoteStartTransaction(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        this.stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
    }

    @Test
    void shouldReleaseConnector() {
        evse.setEvseState(new WaitingForPlugState());

        cancelRemoteStartTransaction.perform(stationStore, stationMessageSenderMock, stateManager);

        verify(stationMessageSenderMock).sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, ConnectorStatus.AVAILABLE);
        verify(stationMessageSenderMock).sendTransactionEventEnded(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReason.EV_CONNECT_TIMEOUT, Reason.TIMEOUT, 0L);
        assertEquals(evse.getEvseState().getStateName(), AvailableState.NAME);
        assertEquals(evse.getTransaction().getStatus(), STOPPED);
    }

    @Test
    void verifyTransactionStatusNotification() {
        evse.setEvseState(new ChargingState());

        cancelRemoteStartTransaction.perform(stationStore, stationMessageSenderMock, stateManager);
        verify(stationMessageSenderMock, times(0)).sendStatusNotification(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, ConnectorStatus.AVAILABLE);

    }

}
