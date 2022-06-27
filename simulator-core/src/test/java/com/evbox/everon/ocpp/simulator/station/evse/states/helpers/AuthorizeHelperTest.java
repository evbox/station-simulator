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

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_HEARTBEAT_INTERVAL;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.UNPLUGGED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthorizeHelperTest {
    private Evse evse;

    private StateManager stateManager;

    @Mock
    private StationMessageSender stationMessageSenderMock;

    private StationStore stationStore;

    @BeforeEach
    void setUp() {
        evse = new Evse(1, Collections.singletonList(new Connector(1, CableStatus.PLUGGED, ConnectorStatus.OCCUPIED)));
        evse.createTransaction("123");
        stationStore = new StationStore(Clock.systemUTC(), DEFAULT_HEARTBEAT_INTERVAL, 100,
                Map.of(DEFAULT_EVSE_ID, new Evse(DEFAULT_EVSE_ID, List.of(new Connector(1, UNPLUGGED, AVAILABLE)))));
        stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
    }

    @Test
    void testDeathorizeWithStopPointWithoutAuthorized() {
        stationStore.setTxStopPointValues(new OptionList<>(emptyList()));

        AuthorizeHelper.handleFailedAuthorizeResponse(stateManager, evse);

        verify(stationMessageSenderMock).sendTransactionEventUpdate(eq(1),
                eq(1),
                eq(TriggerReason.DEAUTHORIZED),
                eq(ChargingState.SUSPENDED_EVSE),
                anyLong());
    }

    @Test
    void testDeathorizeWithStopPointWithAuthorized() {
        stationStore.setTxStopPointValues(new OptionList<>(singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        AuthorizeHelper.handleFailedAuthorizeResponse(stateManager, evse);

        verify(stationMessageSenderMock).sendTransactionEventEnded(eq(1),
                eq(1),
                eq(TriggerReason.DEAUTHORIZED),
                eq(Reason.DE_AUTHORIZED),
                eq(0L));
    }
}
