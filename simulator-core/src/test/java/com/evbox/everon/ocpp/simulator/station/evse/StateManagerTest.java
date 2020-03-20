package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.common.OptionList;
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
    private ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
    private final AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                                                            .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED))
                                                            .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));
    private final AuthorizeResponse notAuthorizeResponse = new AuthorizeResponse()
                                                            .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.INVALID))
                                                            .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));

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
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());

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
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        triggerAuthorizeAndGetResponse();

        checkStateIs(WaitingForPlugState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());

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
    void verifyNotAuthorizedInChargingState() {
        when(evseMock.getEvseState()).thenReturn(new ChargingState());
        checkStateDidNotChangeAfterAuth();
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
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(WaitingForAuthorizationState.NAME);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        triggerAuthorizeAndGetResponse();

        checkStateIs(ChargingState.NAME);
        when(evseMock.getEvseState()).thenReturn(new ChargingState());

        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock, times(2)).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());
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
        when(evseMock.findConnector(anyInt())).thenReturn(new Connector(DEFAULT_CONNECTOR_ID, CableStatus.UNPLUGGED, StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stateManager.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        stateManager.cableUnplugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        checkStateIs(AvailableState.NAME);
    }

    private void triggerAuthorizeAndGetResponse() {
        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock, atLeastOnce()).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());
        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);
    }

    private void checkStateDidNotChangeAfterAuth() {
        stateManager.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());
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
