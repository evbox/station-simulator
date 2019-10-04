package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.mock.factory.EvseCreator;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.StationStateFlowManager;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.states.StoppedState;
import com.evbox.everon.ocpp.simulator.station.states.WaitingForAuthorizationState;
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

import java.util.Collections;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnplugTest {

    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    StationStateFlowManager stationStateFlowManagerMock;

    Unplug unplug;

    @BeforeEach
    void setUp() {
        this.unplug = new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        this.stationStateFlowManagerMock = new StationStateFlowManager(null, stationStoreMock, stationMessageSenderMock);
        this.stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new AvailableState());
    }

    @Test
    void shouldThrowExceptionWhenStateIsLocked() {

        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new WaitingForAuthorizationState());

        Evse evse = mock(Evse.class, RETURNS_DEEP_STUBS);
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evse);
        when(evse.findConnector(anyInt()).getCableStatus()).thenReturn(CableStatus.LOCKED);

        assertThrows(IllegalStateException.class, () -> unplug.perform(stationStateFlowManagerMock));

    }

    @Test
    void verifyTransactionStatusNotification() {

        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new StoppedState());

        when(stationStoreMock.findEvse(anyInt())).thenReturn(EvseCreator.DEFAULT_EVSE_INSTANCE);
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));

        unplug.perform(stationStateFlowManagerMock);

        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventEnded(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                nullable(TransactionData.StoppedReason.class));

    }

}
