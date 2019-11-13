package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationHardwareData;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignCertificateRequestHandlerTest {

    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Captor
    ArgumentCaptor<String> messageCaptor;

    @InjectMocks
    SignCertificateRequestHandler requestHandler;

    @Test
    void verifyCSRCorrectlyCreated() throws Exception {
        requestHandler.run();

        verify(stationStoreMock).setStationPublicKey(any());
        verify(stationStoreMock).setStationPrivateKey(any());
        verify(stationMessageSenderMock).sendSignCertificateRequest(messageCaptor.capture());

        String csr = messageCaptor.getValue();
        PKCS10CertificationRequest certificationRequest = getCSR(csr);
        assertThat(certificationRequest).isNotNull();
        assertThat(certificationRequest.getSubject().getRDNs()[0].getFirst().getValue().toASN1Primitive().toString()).isEqualTo(StationHardwareData.SERIAL_NUMBER);
    }

    private PKCS10CertificationRequest getCSR(String csrEntry) throws Exception {
        PKCS10CertificationRequest csr = null;
        try (ByteArrayInputStream pemStream = new ByteArrayInputStream(csrEntry.getBytes(StandardCharsets.UTF_8))) {
            Reader pemReader = new BufferedReader(new InputStreamReader(pemStream, StandardCharsets.UTF_8));
            PEMParser pemParser = new PEMParser(pemReader);
            Object parsedObj = pemParser.readObject();

            if (parsedObj instanceof PKCS10CertificationRequest) {
                csr = (PKCS10CertificationRequest) parsedObj;
            }
        }

        return csr;
    }

}