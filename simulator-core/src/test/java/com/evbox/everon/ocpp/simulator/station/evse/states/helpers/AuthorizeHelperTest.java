package com.evbox.everon.ocpp.simulator.station.evse.states.helpers;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.v201.message.station.ChargingState;
import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizeHelperTest {
    private Evse evse;

    @Mock
    private StateManager stateManagerMock;

    @Mock
    private StationMessageSender stationMessageSenderMock;

    @Mock
    private StationStore stationStoreMock;

    @BeforeEach
    void setUp() {
        evse = new Evse(1, Collections.singletonList(new Connector(1, CableStatus.PLUGGED, ConnectorStatus.OCCUPIED)));
        evse.createTransaction("123");

        when(stateManagerMock.getStationMessageSender()).thenReturn(stationMessageSenderMock);
        when(stateManagerMock.getStationStore()).thenReturn(stationStoreMock);
    }

    @Test
    void testDeathorizeWithStopPointWithoutAuthorized() {
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(emptyList()));

        AuthorizeHelper.handleFailedAuthorizeResponse(stateManagerMock, evse);

        verify(stationMessageSenderMock).sendTransactionEventUpdate(eq(1),
                eq(1),
                eq(TriggerReason.DEAUTHORIZED),
                eq(ChargingState.SUSPENDED_EVSE),
                anyLong());
    }

    @Test
    void testDeathorizeWithStopPointWithAuthorized() {
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        AuthorizeHelper.handleFailedAuthorizeResponse(stateManagerMock, evse);

        verify(stationMessageSenderMock).sendTransactionEventEnded(eq(1),
                eq(1),
                eq(TriggerReason.DEAUTHORIZED),
                eq(Reason.DE_AUTHORIZED),
                eq(0L));
    }
}
