package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v201.message.station.CertificateSignedRequest;
import com.evbox.everon.ocpp.v201.message.station.CertificateSignedResponse;
import com.evbox.everon.ocpp.v201.message.station.CertificateSignedStatus;
import com.evbox.everon.ocpp.v201.message.station.CertificateSigningUse;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CertificateSignedRequestHandlerTest {

    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Captor
    ArgumentCaptor<CertificateSignedResponse> messageCaptor;

    @InjectMocks
    CertificateSignedRequestHandler requestHandler;

    private static String expiredCertificate;
    // Following certificates valid until 5 November 2029
    private static String wrongSerialCertificate;
    private static String validCertificate;

    @BeforeAll
    static void loadCertificates() throws IOException {
        expiredCertificate = Resources.toString(Resources.getResource("derCertificates/expiredCertificate.der"), Charsets.UTF_8);
        wrongSerialCertificate = Resources.toString(Resources.getResource("derCertificates/wrongSerialCertificate.der"), Charsets.UTF_8);
        validCertificate = Resources.toString(Resources.getResource("derCertificates/validCertificate.der"), Charsets.UTF_8);
    }

    @Test
    @DisplayName("Expired certificate should be rejected")
    void verifyExpiredCertificateIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCertificateChain(new CiString.CiString10000(expiredCertificate)));//TODO check that certificate is actually a "chain"

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedStatus.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("Empty certificate should be rejected")
    void verifyEmptyCertificateIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCertificateChain(new CiString.CiString10000("NotACert")));//TODO check that certificate is actually a "chain"

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedStatus.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("Certificate with wrong serial number should be rejected")
    void verifyCertificateWithInvalidSerialIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCertificateChain(new CiString.CiString10000(wrongSerialCertificate)));//TODO check that certificate is actually a "chain"

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedStatus.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("V2G certificate type should be rejected")
    void verifyV2GCertificateTypeIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCertificateType(CertificateSigningUse.V_2_G_CERTIFICATE).withCertificateChain(new CiString.CiString10000(wrongSerialCertificate)));//TODO check that certificate is actually a "chain"

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedStatus.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("Correct certificate type should be accepted")
    void verifyValidCertificateTypeIsCorrectlySet() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCertificateChain(new CiString.CiString10000(validCertificate)));//TODO check that certificate is actually a "chain"

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedStatus.ACCEPTED.value());
        verify(stationStoreMock).setStationCertificate(any());
        verify(stationStoreMock).setStationCertificateChain(argThat(chain -> chain.size() == 1));

        ScheduledFuture scheduledFuture = requestHandler.getScheduledFuture();
        assertThat(Optional.ofNullable(scheduledFuture)).isNotEmpty();

        Instant triggerInstant = Instant.now().plusSeconds(scheduledFuture.getDelay(TimeUnit.SECONDS));
        ZonedDateTime triggerDate = ZonedDateTime.ofInstant(triggerInstant, ZoneId.systemDefault());
        assertThat(triggerDate.getDayOfMonth()).isEqualTo(5);
        assertThat(triggerDate.getMonthValue()).isEqualTo(11);
        assertThat(triggerDate.getYear()).isEqualTo(2029);
    }

    //TODO consider removing given new certificate chain parameter OCPP 2.0 to OCPP 2.0.1
    @SuppressWarnings("unused")
    private List<CiString.CiString5500> stringToCiStringsList(String certificate) {
        List<CiString.CiString5500> result = new ArrayList<>();
        Arrays.stream(certificate.split("\n")).forEach(c -> result.add(new CiString.CiString5500(c)));
        return result;
    }

}
