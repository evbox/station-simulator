package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v201.message.station.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new NetworkConnectionProfile().withOcppVersion(OCPPVersion.OCPP_20).withOcppTransport(OCPPTransport.JSON)));

        verify(stationStore).addNetworkConnectionProfile(anyInt(), any(NetworkConnectionProfile.class));
        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());

        SetNetworkProfileResponse response = responseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(SetNetworkProfileStatus.ACCEPTED);
    }

    @Test
    public void testSetNetworkProfileOcppVersionIsMissing() {
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new NetworkConnectionProfile().withOcppTransport(OCPPTransport.JSON)));

        assertRejected();
    }

    @Test
    public void testSetNetworkProfileOcppVersionIsNot20() {
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new NetworkConnectionProfile().withOcppVersion(OCPPVersion.OCPP_16).withOcppTransport(OCPPTransport.JSON)));

        assertRejected();
    }

    @Test
    public void testSetNetworkProfileOcppTransportIsSoap() {
        handler.handle(CALL_ID, new SetNetworkProfileRequest().withConfigurationSlot(CONFIGURATION_SLOT).withConnectionData(new NetworkConnectionProfile().withOcppVersion(OCPPVersion.OCPP_16).withOcppTransport(OCPPTransport.SOAP)));

        assertRejected();
    }

    private void assertRejected() {
        verifyZeroInteractions(stationStore);
        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());

        SetNetworkProfileResponse response = responseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(SetNetworkProfileStatus.REJECTED);
    }
}
