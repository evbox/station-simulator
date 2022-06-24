package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.StoppedState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForAuthorizationState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v201.message.station.StatusNotificationResponse;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.*;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnplugTest {

    Connector connector;
    Evse evse;
    StationStore stationStore;
    @Mock
    StationMessageSender stationMessageSenderMock;
    StateManager stateManager;

    Unplug unplug;

    @BeforeEach
    void setUp() {
        connector = new Connector(1, UNPLUGGED, AVAILABLE);
        evse = new Evse(DEFAULT_EVSE_ID, List.of(connector));
        evse.setEvseState(new AvailableState());
        stationStore = new StationStore(Clock.systemUTC(), 10, 100, Map.of(DEFAULT_EVSE_ID, evse));
        this.unplug = new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        this.stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
    }

    @Test
    void shouldThrowExceptionWhenStateIsLocked() {
        evse.setEvseState(new WaitingForAuthorizationState());
        connector.plug();
        connector.lock();

        assertThrows(IllegalStateException.class, () -> unplug.perform(stateManager));

    }

    @Test
    void verifyTransactionStatusNotification() {
        evse.setEvseState(new StoppedState());
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));

        unplug.perform(stateManager);

        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventEnded(anyInt(), anyInt(), any(TriggerReason.class),
                nullable(Reason.class), anyLong());

    }

}
