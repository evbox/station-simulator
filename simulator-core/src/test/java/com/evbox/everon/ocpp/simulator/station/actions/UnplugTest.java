package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.simulator.support.EvseCreator;
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

import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_EVSE_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UnplugTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;

    private Unplug unplug;

    @BeforeEach
    void setUp() {
        this.unplug = new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
    }

    @Test
    void shouldThrowExceptionWhenStateIsLocked() {

        when(stationStateMock.getCableStatus(anyInt(), anyInt())).thenReturn(CableStatus.LOCKED);

        assertThrows(IllegalStateException.class, () -> unplug.perform(stationStateMock, stationMessageSenderMock));

    }

    @Test
    void verifyTransactionStatusNotification() {

        when(stationStateMock.findEvse(anyInt())).thenReturn(EvseCreator.DEFAULT_EVSE_INSTANCE);

        unplug.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(Evse.class), any(Connector.class), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventEnded(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                any(TransactionData.StoppedReason.class));

    }
}
