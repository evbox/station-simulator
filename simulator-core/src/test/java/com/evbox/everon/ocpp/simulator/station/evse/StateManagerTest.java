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

import java.util.Collections;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateManagerTest {

    @Mock
    Evse evseMock;

    @Mock
    StationStore stationStoreMock;

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
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.getEvseState()).thenReturn(new AvailableState());
        this.stateManager = new StateManager(null, stationStoreMock, stationMessageSenderMock);
    }

    @Test
    void verifyFullStateFlowPlugThenAuthorize() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.emptyList()));
        when(stationStoreMock.isAuthEnabled()).thenReturn(true);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());
        when(evseMock.getTokenId()).thenReturn(DEFAULT_TOKEN_ID);

        triggerAuthorizeAndGetResponse();

        checkStateIs(StoppedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new StoppedState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyFullStateFlowAuthorizeThenPlug() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.emptyList()));
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.emptyList()));
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, ConnectorStatus.AVAILABLE));

        triggerAuthorizeAndGetResponse();

        checkStateIs(WaitingForPlugState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());
        when(evseMock.getTokenId()).thenReturn(DEFAULT_TOKEN_ID);

        triggerAuthorizeAndGetResponse();

        checkStateIs(StoppedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new StoppedState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyNotAuthorizedInAvailableState() {
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyNotAuthorizedInWaitingForAuthorizationState() {
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyNotAuthorizedInWaitingForPlugState() {
        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyDeathorizeInChargingStateSwitchToStopped() {
        when(evseMock.getEvseState()).thenReturn(new ChargingState());
        when(evseMock.hasOngoingTransaction()).thenReturn(true);
        when(evseMock.getTokenId()).thenReturn(DEFAULT_TOKEN_ID);
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), notAuthorizeResponse);

        // Verify that state did not change
        verify(evseMock).setEvseState(any(StoppedState.class));
        verify(evseMock).tryUnlockConnector();
    }

    @Test
    void verifyDeathorizeInChargingStateSwitchToWaitingForAutorization() {
        when(evseMock.getEvseState()).thenReturn(new ChargingState());
        when(evseMock.hasOngoingTransaction()).thenReturn(true);
        when(evseMock.getTokenId()).thenReturn(DEFAULT_TOKEN_ID);
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.emptyList()));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), notAuthorizeResponse);

        // Verify that state did not change
        verify(evseMock).setEvseState(any(WaitingForAuthorizationState.class));
        verify(evseMock).tryUnlockConnector();
    }

    @Test
    void verifyNotAuthorizedInStoppedState() {
        when(evseMock.getEvseState()).thenReturn(new StoppedState());
        checkStateDidNotChangeAfterAuth();
    }

    @Test
    void verifyStopChargingAndRestart() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationStoreMock.getTxStopPointValues()).thenReturn(new OptionList<>(Collections.emptyList()));
        when(stationStoreMock.isAuthEnabled()).thenReturn(true);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());
        when(evseMock.getTokenId()).thenReturn(DEFAULT_TOKEN_ID);

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock, times(2)).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        checkStateIs(StoppedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new StoppedState());

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME, 2);
    }

    @Test
    void verifyAuthorizeAndAuthorize() {
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.emptyList()));

        triggerAuthorizeAndGetResponse();

        checkStateIs(WaitingForPlugState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        triggerAuthorizeAndGetResponse();

        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyPlugAndUnplug() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyFullStateAutostartFlow() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationStoreMock.isAuthEnabled()).thenReturn(false);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(EvDisconnectedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new EvDisconnectedState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyFullStateAutostartWithRemoteStopFlow() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationStoreMock.isAuthEnabled()).thenReturn(false);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(EvDisconnectedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new EvDisconnectedState());

        stateManager.remoteStop(DEFAULT_EVSE_ID);
        checkStateIs(RemotelyStoppedState.NAME);
    }

    private void triggerAuthorizeAndGetResponse() {
        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock, atLeastOnce()).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);
    }

    private void checkStateDidNotChangeAfterAuth() {
        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), notAuthorizeResponse);

        // Verify that state did not change
        verify(evseMock, never()).setEvseState(any());
    }

    private void checkStateIs(String name) {
        checkStateIs(name, 1);
    }

    private void checkStateIs(String name, int times) {
        verify(evseMock, times(times)).setEvseState(argThat(s -> s.getStateName().equals(name)));
    }

}
