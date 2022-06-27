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

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.*;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlugTest {

    StationStore stationStore;
    @Mock
    StationMessageSender stationMessageSenderMock;
    StateManager stateManager;
    Evse evse;
    Connector connector;

    Plug plug;

    @BeforeEach
    void setUp() {
        this.plug = new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        connector = new Connector(1, UNPLUGGED, AVAILABLE);
        evse = new Evse(DEFAULT_EVSE_ID, List.of(connector));
        evse.setEvseState(new AvailableState());
        stationStore = new StationStore(Clock.systemUTC(), 10, 100, Map.of(DEFAULT_EVSE_ID, evse));
        this.stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
    }

    @Test
    void shouldFutureFailOnIllegalState() throws Exception {
        connector.plug();
        connector.lock();

        assertEquals(UserMessageResult.FAILED, plug.perform(stateManager).get());

    }

    @Test
    void verifyTransactionEventUpdate() {

        // given
        evse.setEvseState(new WaitingForPlugState());
        evse.createTransaction("1");

        // when
        plug.perform(stateManager);

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
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.EV_CONNECTED)));

        // when
        plug.perform(stateManager);

        // then
        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventStart(anyInt(), anyInt(), any(TriggerReason.class),
                any(ChargingState.class));

    }
}
