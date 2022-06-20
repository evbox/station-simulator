package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.states.ChargingState;
import com.evbox.everon.ocpp.simulator.station.evse.states.*;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v201.message.station.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.*;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateManagerTest {

    Evse evse;

    Connector connector;

    StationStore stationStore;

    @Mock
    StationMessageSender stationMessageSenderMock;

    private StateManager stateManager;

    @Captor
    private ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor;

    private final AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                                                            .withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)
                                                            .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID)));
    private final AuthorizeResponse notAuthorizeResponse = new AuthorizeResponse()
                                                            .withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.INVALID));

    @BeforeEach
    void setUp() {
        connector = new Connector(1, UNPLUGGED, AVAILABLE);
        evse = new Evse(DEFAULT_EVSE_ID, List.of(connector));
        stationStore = new StationStore(Clock.systemUTC(), 10, 100,
                Map.of(DEFAULT_EVSE_ID, evse));
        this.stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
    }

    @Test
    void verifyFullStateFlowPlugThenAuthorize() {
        stationStore.setTxStopPointValues(new OptionList<>(Collections.emptyList()));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME);
        evse.setTokenId(DEFAULT_TOKEN_ID);

        triggerAuthorizeAndGetResponse();

        checkStateIs(StoppedState.NAME);

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyFullStateFlowAuthorizeThenPlug() {
        stationStore.setTxStartPointValues(new OptionList<>(Collections.emptyList()));
        stationStore.setTxStopPointValues(new OptionList<>(Collections.emptyList()));

        triggerAuthorizeAndGetResponse();

        checkStateIs(WaitingForPlugState.NAME);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(ChargingState.NAME);
        evse.setToken(DEFAULT_TOKEN_ID);
        evse.lockPluggedConnector();

        triggerAuthorizeAndGetResponse();

        checkStateIs(StoppedState.NAME);

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyNotAuthorizedInAvailableState() {
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyNotAuthorizedInWaitingForAuthorizationState() {
        evse.setEvseState(new WaitingForAuthorizationState());
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyNotAuthorizedInWaitingForPlugState() {
        evse.setEvseState(new WaitingForPlugState());
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyDeathorizeInChargingStateSwitchToStopped() {
        connector.setCableStatus(LOCKED);
        evse.setEvseState(new ChargingState());
        evse.createTransaction("123");
        evse.setToken(DEFAULT_TOKEN_ID);
        stationStore.setTxStopPointValues(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), notAuthorizeResponse);

        // Verify that state did not change
        checkStateIs(StoppedState.NAME);
        assertEquals(PLUGGED, connector.getCableStatus());
    }

    @Test
    void verifyDeathorizeInChargingStateSwitchToWaitingForAutorization() {
        connector.setCableStatus(LOCKED);
        evse.setEvseState(new ChargingState());
        evse.createTransaction("123");
        evse.setToken(DEFAULT_TOKEN_ID);
        stationStore.setTxStopPointValues(new OptionList<>(Collections.emptyList()));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), notAuthorizeResponse);

        checkStateIs(WaitingForAuthorizationState.NAME);
        assertEquals(PLUGGED, connector.getCableStatus());
    }

    @Test
    void verifyNotAuthorizedInStoppedState() {
        evse.setEvseState(new StoppedState());
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyStopChargingAndRestart() {
        stationStore.setTxStopPointValues(new OptionList<>(Collections.emptyList()));
        stationStore.setAuthorizeState(true);

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);
        evse.setToken(DEFAULT_TOKEN_ID);

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME);

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock, times(2)).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        checkStateIs(StoppedState.NAME);

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME);
    }

    @Test
    void verifyAuthorizeAndAuthorize() {
        stationStore.setTxStartPointValues(new OptionList<>(Collections.emptyList()));

        triggerAuthorizeAndGetResponse();

        checkStateIs(WaitingForPlugState.NAME);

        triggerAuthorizeAndGetResponse();

        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyPlugAndUnplug() {
        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);

        evse.setEvseState(new WaitingForAuthorizationState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);

        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyFullStateAutostartFlow() {
        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(EvDisconnectedState.NAME);

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyFullStateAutostartWithRemoteStopFlow() {
        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(EvDisconnectedState.NAME);

        stateManager.remoteStop(DEFAULT_EVSE_ID);
        checkStateIs(RemotelyStoppedState.NAME);
    }

    private void triggerAuthorizeAndGetResponse() {
        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock, atLeastOnce()).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);
    }

    private void checkStateDidNotChangeAfterAuth() {
        AbstractEvseState expectedState = evse.getEvseState();

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), notAuthorizeResponse);

        // Verify that state did not change
        assertEquals(expectedState, evse.getEvseState());
    }

    private void checkStateIs(String name) {
        assertEquals(name, evse.getEvseState().getStateName());
    }

}
