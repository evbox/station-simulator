package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.ChargingState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForAuthorizationState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v201.message.station.AuthorizationStatus;
import com.evbox.everon.ocpp.v201.message.station.AuthorizeRequest;
import com.evbox.everon.ocpp.v201.message.station.AuthorizeResponse;
import com.evbox.everon.ocpp.v201.message.station.IdTokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TOKEN_ID;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.PLUGGED;
import static com.evbox.everon.ocpp.simulator.station.evse.CableStatus.UNPLUGGED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.OCCUPIED;
import static org.assertj.core.util.Lists.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizeTest {

    StationStore stationStore;
    @Mock
    StationMessageSender stationMessageSenderMock;
    StateManager stateManager;
    Evse evse;

    Authorize authorize;

    private AuthorizeResponse authorizeResponse;

    private static final Integer EVSE_ID = 1;

    @BeforeEach
    void setUp() {
        this.authorize = new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID);
        evse = new Evse(DEFAULT_EVSE_ID, emptyList());
        evse.setEvseState(new AvailableState());
        stationStore = new StationStore(Clock.systemUTC(), 10, 100, Map.of(DEFAULT_EVSE_ID, evse));
        stationStore.setTxStartPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED)));
        stationStore.setTxStopPointValues(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED)));
        this.stateManager = new StateManager(null, stationStore, stationMessageSenderMock);
        authorizeResponse = new AuthorizeResponse()
                .withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatus.ACCEPTED)
                .withEvseId(Collections.singletonList(EVSE_ID))
                );

    }

    @Test
    void shouldSetToken() {
        authorize.perform(stateManager);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);
        assertNotNull(evse.getTokenId());

    }

    @Test
    void shouldSetTransactionId() {
        authorize.perform(stateManager);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);
        assertNotNull(evse.getTransaction());

    }

    @Test
    void shouldSetStateToStartCharging_WhenPlugged() {
        evse = new Evse(DEFAULT_EVSE_ID, List.of(new Connector(1, PLUGGED, OCCUPIED)));
        evse.setEvseState(new WaitingForAuthorizationState());
        authorize.perform(stateManager);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);
    }

    @Test
    void shouldSetStateToStopCharging_When_IsNotPlugged_AndStateIsCharging() {
        evse = new Evse(DEFAULT_EVSE_ID, List.of(new Connector(1, UNPLUGGED, AVAILABLE)));
        evse.setEvseState(new ChargingState());
        evse.setToken(DEFAULT_TOKEN_ID);

        authorize.perform(stateManager);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);
        assertFalse(evse.isCharging());

    }

    @Test
    void shouldNotSetState_WhenIsNotPlugged_AndStateIsNotCharging_AndHasNoOngoingTransaction() {
        authorize.perform(stateManager);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), subscriberCaptor.capture());

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

    }

    @Test
    void shouldNotStopWithDifferentToken() {
        final String wrongToken = "AA123";
        evse.setEvseState(new ChargingState());
        evse.setToken(DEFAULT_TOKEN_ID);
        new Authorize(wrongToken, DEFAULT_EVSE_ID).perform(stateManager);

        verify(stationMessageSenderMock, never()).sendAuthorizeAndSubscribe(anyString(), any());
    }
}
