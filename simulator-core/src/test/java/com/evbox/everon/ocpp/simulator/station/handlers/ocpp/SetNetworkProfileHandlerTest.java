package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v20.message.station.ConnectionData;
import com.evbox.everon.ocpp.v20.message.station.SetNetworkProfileRequest;
import com.evbox.everon.ocpp.v20.message.station.SetNetworkProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@ExtendWith(MockitoExtension.class)
class SetNetworkProfileHandlerTest {

    private static final String CALL_ID = "123";
    private static final int CONFIGURATION_SLOT = 123;

    @Mock
    private StationMessageSender stationMessageSender;

    @Mock
    private StationStore stationStore;

    @InjectMocks
    private SetNetworkProfileHandler handler;

    @Captor
    private ArgumentCaptor<SetNetworkProfileResponse> responseCaptor = ArgumentCaptor.forClass(SetNetworkProfileResponse.class);

    @Test
    public void testSetNetworkProfileValidRequest() {
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new ConnectionData().withOcppVersion(ConnectionData.OcppVersion.OCPP_20).withOcppTransport(ConnectionData.OcppTransport.JSON)));

        verify(stationStore).addNetworkConnectionProfile(anyInt(), any(ConnectionData.class));
        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());

        SetNetworkProfileResponse response = responseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(SetNetworkProfileResponse.Status.ACCEPTED);
        assertThat(response.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void testSetNetworkProfileOcppVersionIsMissing() {
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new ConnectionData().withOcppTransport(ConnectionData.OcppTransport.JSON)));

        assertRejected();
    }

    @Test
    public void testSetNetworkProfileOcppVersionIsNot20() {
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new ConnectionData().withOcppVersion(ConnectionData.OcppVersion.OCPP_16).withOcppTransport(ConnectionData.OcppTransport.JSON)));

        assertRejected();
    }

    @Test
    public void testSetNetworkProfileOcppTransportIsSoap() {
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new ConnectionData().withOcppVersion(ConnectionData.OcppVersion.OCPP_16).withOcppTransport(ConnectionData.OcppTransport.SOAP)));

        assertRejected();
    }

    private void assertRejected() {
        verifyZeroInteractions(stationStore);
        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());

        SetNetworkProfileResponse response = responseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(SetNetworkProfileResponse.Status.REJECTED);
        assertThat(response.getAdditionalProperties()).isEmpty();
    }
}
