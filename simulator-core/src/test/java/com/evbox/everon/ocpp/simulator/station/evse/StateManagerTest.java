package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.states.*;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @BeforeEach
    void setUp() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.getEvseState()).thenReturn(new AvailableState());
        this.stateManager = new StateManager(null, stationStoreMock, stationMessageSenderMock);
    }

    @Test
    void verifyFullStateFlowPlugThenAuthorize() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(StoppedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new StoppedState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyFullStateFlowAuthorizeThenPlug() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(WaitingForPlugState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(StoppedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new StoppedState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyStopChargingAndRestart() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(StoppedState.NAME);
        when(evseMock.getEvseState()).thenReturn(new StoppedState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock, times(3)).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());


        AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED))
                .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        checkStateIs(ChargingState.NAME, 2);
    }

    @Test
    void verifyAuthorizeAndAuthorize() {
        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(WaitingForPlugState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        checkStateIs(AvailableState.NAME);
    }

    @Test
    void verifyPlugAndUnplug() {
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    private void checkStateIs(String name) {
        checkStateIs(name, 1);
    }

    private void checkStateIs(String name, int times) {
        verify(evseMock, times(times)).setEvseState(argThat(s -> s.getStateName().equals(name)));
    }

}
