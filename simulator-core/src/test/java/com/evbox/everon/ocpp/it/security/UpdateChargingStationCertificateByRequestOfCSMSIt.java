package com.evbox.everon.ocpp.it.security;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.SignCertificate;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.*;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateChargingStationCertificateByRequestOfCSMSIt extends StationSimulatorSetUp {

    @Test
    void shouldRequestNewCertificate() throws IOException {
        final String certificate = Resources.toString(Resources.getResource("derCertificates/validCertificate.der"), Charsets.UTF_8);
        final String pemCertificate = Resources.toString(Resources.getResource("pemCertificates/validCertificate.pem"), Charsets.UTF_8).replaceAll("\n", "");

        ocppMockServer
                .when(SignCertificate.request())
                .thenReturn(SignCertificate.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        TriggerMessageRequest triggerMessageRequest = new TriggerMessageRequest()
                                                            .withRequestedMessage(TriggerMessageRequest.RequestedMessage.SIGN_CHARGING_STATION_CERTIFICATE);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.TRIGGER_MESSAGE, triggerMessageRequest);
        TriggerMessageResponse triggerResponse = ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), TriggerMessageResponse.class);

        assertThat(triggerResponse).isNotNull();
        assertThat(triggerResponse.getStatus().value()).isEqualTo(TriggerMessageResponse.Status.ACCEPTED.value());

        CertificateSignedRequest certificateSignedRequest = new CertificateSignedRequest().withCert(stringToCiStringsList(certificate));
        call = new Call(DEFAULT_CALL_ID, ActionType.CERTIFICATE_SIGNED, certificateSignedRequest);
        CertificateSignedResponse certificateResponse = ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), CertificateSignedResponse.class);

        assertThat(certificateResponse).isNotNull();
        assertThat(certificateResponse.getStatus().value()).isEqualTo(CertificateSignedResponse.Status.ACCEPTED.value());

        String storedCertificate = stationSimulatorRunner.getStation(STATION_ID).getStateView().getStationCertificate();
        assertThat(storedCertificate.replaceAll("\n", "")).isEqualTo(pemCertificate);
    }

    private List<CiString.CiString5500> stringToCiStringsList(String certificate) {
        List<CiString.CiString5500> result = new ArrayList<>();
        Splitter.fixedLength(5500).split(certificate).forEach(c -> result.add(new CiString.CiString5500(c)));
        return result;
    }

}
