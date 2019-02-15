package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.AuthorizeRequest;
import com.evbox.everon.ocpp.v20.message.station.AuthorizeResponse;
import com.evbox.everon.ocpp.v20.message.station.IdTokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_TOKEN_ID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizeTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;

    Authorize authorize;

    @BeforeEach
    void setUp() {
        this.authorize = new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID);
    }

    @Test
    void shouldStoreToken() {

        authorize.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED));

        when(stationStateMock.getDefaultEvseId()).thenReturn(DEFAULT_EVSE_ID);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(stationStateMock).storeToken(anyInt(), anyString());

    }

    @Test
    void shouldSetTransactionId() {

        authorize.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED));

        when(stationStateMock.getDefaultEvseId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStateMock.hasOngoingTransaction(anyInt())).thenReturn(false);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(stationStateMock).setTransactionId(anyInt(), anyInt());

    }

    @Test
    void shouldSetStateToStartCharging_WhenPlugged() {

        authorize.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED));

        when(stationStateMock.getDefaultEvseId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStateMock.hasOngoingTransaction(anyInt())).thenReturn(false);
        when(stationStateMock.isPlugged(anyInt())).thenReturn(true);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(stationStateMock).startCharging(anyInt());

    }

    @Test
    void shouldSetStateToStopCharging_When_IsNotPlugged_AndStateIsCharging() {

        authorize.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED));

        when(stationStateMock.getDefaultEvseId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStateMock.hasOngoingTransaction(anyInt())).thenReturn(false);
        when(stationStateMock.isPlugged(anyInt())).thenReturn(false);
        when(stationStateMock.isCharging(anyInt())).thenReturn(true);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(stationStateMock).stopCharging(anyInt());

    }

    @Test
    void shouldSetStateToStartCharging_WhenIsNotPlugged_AndStateIsNotCharging_AndHasOngoingTransaction() {

        authorize.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED));

        when(stationStateMock.getDefaultEvseId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStateMock.hasOngoingTransaction(anyInt())).thenReturn(true);
        when(stationStateMock.isPlugged(anyInt())).thenReturn(false);
        when(stationStateMock.isCharging(anyInt())).thenReturn(false);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(stationStateMock).startCharging(anyInt());

    }

    @Test
    void shouldNotSetState_WhenIsNotPlugged_AndStateIsNotCharging_AndHasNoOngoingTransaction() {

        authorize.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED));

        when(stationStateMock.getDefaultEvseId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStateMock.hasOngoingTransaction(anyInt())).thenReturn(false);
        when(stationStateMock.isPlugged(anyInt())).thenReturn(false);
        when(stationStateMock.isCharging(anyInt())).thenReturn(false);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(stationStateMock, never()).startCharging(anyInt());
        verify(stationStateMock, never()).stopCharging(anyInt());

    }
}
