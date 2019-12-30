package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.factory.OcppMessageFactory;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForAuthorizationState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForPlugState;
import com.evbox.everon.ocpp.v20.message.common.IdToken;
import com.evbox.everon.ocpp.v20.message.station.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestStartTransactionRequestHandlerTest {

    @Mock
    StationMessageSender stationMessageSender;
    @Mock
    StationStore stationStore;
    @Mock
    StateManager stateManager;

    @Mock
    Evse evse;
    @Mock
    Connector connector;
    @Mock
    EvseTransaction evseTransaction;

    @Captor
    ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);

    @InjectMocks
    RequestStartTransactionRequestHandler handler;

    private RequestStartTransactionRequest request = OcppMessageFactory.createRequestStartTransactionBuilder()
                                                    .withEvseId(DEFAULT_EVSE_ID)
                                                    .withRemoteStartId(123)
                                                    .withIdToken(new IdToken().withIdToken(new CiString.CiString36(DEFAULT_TOKEN_ID)))
                                                    .build();

    @Test
    void shouldStartPluginFirst() {
        when(evse.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evse.hasOngoingTransaction()).thenReturn(true);
        when(evse.getTransaction()).thenReturn(evseTransaction);
        when(evse.tryFindPluggedConnector()).thenReturn(Optional.of(connector));
        when(evseTransaction.getTransactionId()).thenReturn(DEFAULT_TRANSACTION_ID);
        when(stateManager.getStateForEvse(DEFAULT_EVSE_ID)).thenReturn(new WaitingForAuthorizationState());
        when(stationStore.tryFindEvse(DEFAULT_EVSE_ID)).thenReturn(Optional.of(evse));

        handler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        RequestStartTransactionResponse response = (RequestStartTransactionResponse) responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(RequestStartTransactionResponse.Status.ACCEPTED);
    }

    @Test
    void shouldNotStartNoTransactionPluginFirst() {
        when(evse.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evse.hasOngoingTransaction()).thenReturn(false);
        when(stateManager.getStateForEvse(DEFAULT_EVSE_ID)).thenReturn(new WaitingForAuthorizationState());
        when(stationStore.tryFindEvse(DEFAULT_EVSE_ID)).thenReturn(Optional.of(evse));

        handler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        RequestStartTransactionResponse response = (RequestStartTransactionResponse) responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(RequestStartTransactionResponse.Status.REJECTED);
    }

    @Test
    void shouldNotStartNoEvse() {
        when(stationStore.tryFindEvse(DEFAULT_EVSE_ID)).thenReturn(Optional.empty());

        handler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        RequestStartTransactionResponse response = (RequestStartTransactionResponse) responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(RequestStartTransactionResponse.Status.REJECTED);
    }

    @Test
    void shouldNotStartNoConnector() {
        when(evse.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(evse.tryFindAvailableConnector()).thenReturn(Optional.empty());
        when(stateManager.getStateForEvse(DEFAULT_EVSE_ID)).thenReturn(new AvailableState());
        when(stationStore.tryFindEvse(DEFAULT_EVSE_ID)).thenReturn(Optional.of(evse));

        handler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        RequestStartTransactionResponse response = (RequestStartTransactionResponse) responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(RequestStartTransactionResponse.Status.REJECTED);
    }

    @Test
    void shouldNotStartWrongState() {
        when(evse.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(stateManager.getStateForEvse(DEFAULT_EVSE_ID)).thenReturn(new WaitingForPlugState());
        when(stationStore.tryFindEvse(DEFAULT_EVSE_ID)).thenReturn(Optional.of(evse));

        handler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        RequestStartTransactionResponse response = (RequestStartTransactionResponse) responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(RequestStartTransactionResponse.Status.REJECTED);
    }

}