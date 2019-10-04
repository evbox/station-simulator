package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.*;
import com.evbox.everon.ocpp.simulator.station.actions.user.Authorize;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.ChargingStopReason;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.states.ChargingState;
import com.evbox.everon.ocpp.simulator.station.states.WaitingForAuthorizationState;
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

import java.util.Arrays;
import java.util.Collections;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TOKEN_ID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizeTest {

    @Mock
    StationPersistenceLayer stationPersistenceLayerMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    StationStateFlowManager stationStateFlowManagerMock;
    @Mock
    Evse evseMock;

    Authorize authorize;

    @BeforeEach
    void setUp() {
        this.authorize = new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID);
        this.stationStateFlowManagerMock = new StationStateFlowManager(null, stationPersistenceLayerMock, stationMessageSenderMock);
        this.stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new AvailableState());
    }

    @Test
    void shouldSetToken() {

        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);
        when(stationPersistenceLayerMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED)));

        authorize.perform(stationStateFlowManagerMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED))
                .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(evseMock).setToken(anyString());

    }

    @Test
    void shouldSetTransactionId() {
        when(stationPersistenceLayerMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED)));

        authorize.perform(stationStateFlowManagerMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED))
                .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));

        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(evseMock).createTransaction(anyString());

    }

    @Test
    void shouldSetStateToStartCharging_WhenPlugged() {

        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new WaitingForAuthorizationState());

        authorize.perform(stationStateFlowManagerMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED))
                .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));

        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);
        when(evseMock.hasOngoingTransaction()).thenReturn(false);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(evseMock).startCharging();

    }

    @Test
    void shouldSetStateToStopCharging_When_IsNotPlugged_AndStateIsCharging() {
        when(stationPersistenceLayerMock.getTxStopPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(evseMock.getStopReason()).thenReturn(ChargingStopReason.LOCALLY_STOPPED);

        stationStateFlowManagerMock.setStateForEvse(DEFAULT_EVSE_ID, new ChargingState());

        authorize.perform(stationStateFlowManagerMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED))
                .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));

        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(evseMock).stopCharging();

    }

    @Test
    void shouldNotSetState_WhenIsNotPlugged_AndStateIsNotCharging_AndHasNoOngoingTransaction() {
        when(stationPersistenceLayerMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED)));

        authorize.perform(stationStateFlowManagerMock);

        ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), subscriberCaptor.capture());

        AuthorizeResponse authorizeResponse = new AuthorizeResponse()
                .withIdTokenInfo(new IdTokenInfo().withStatus(IdTokenInfo.Status.ACCEPTED))
                .withEvseId(Collections.singletonList(DEFAULT_EVSE_ID));

        when(stationPersistenceLayerMock.findEvse(anyInt())).thenReturn(evseMock);

        subscriberCaptor.getValue().onResponse(new AuthorizeRequest(), authorizeResponse);

        verify(evseMock, never()).startCharging();
        verify(evseMock, never()).stopCharging();

    }
}
