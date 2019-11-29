package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationHardwareData;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v20.message.station.CertificateSignedRequest;
import com.evbox.everon.ocpp.v20.message.station.CertificateSignedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handler for {@link CertificateSignedRequest} request.
 */
@Slf4j
public class CertificateSignedRequestHandler implements OcppRequestHandler<CertificateSignedRequest> {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5);

    private final StationStore stationStore;
    private final StationMessageSender stationMessageSender;

    private ScheduledFuture scheduledFuture;

    public CertificateSignedRequestHandler(StationStore stationStore, StationMessageSender stationMessageSender) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
    }

    @Override
    public void handle(String callId, CertificateSignedRequest request) {
        if (request.getTypeOfCertificate() == CertificateSignedRequest.TypeOfCertificate.V_2_G_CERTIFICATE) {
            stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedResponse.Status.REJECTED));
            return;
        }

        List<String> chain = new ArrayList<>();
        request.getCert().forEach(c -> chain.add(c.toString()));

        if (chain.isEmpty()) {
            stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedResponse.Status.REJECTED));
            return;
        }


        X509Certificate stationCertificate = convertStringToCertificate(chain.get(0));
        if (stationCertificate != null && isCertificateValid(stationCertificate)) {
            stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedResponse.Status.ACCEPTED));
            stationStore.setStationCertificate(stationCertificate);
            startCertificateRenewerTask(stationCertificate);

            if (chain.size() > 1) {
                List<X509Certificate> stationCertificateChain = new ArrayList<>();
                chain.subList(1, chain.size()).forEach(c -> stationCertificateChain.add(convertStringToCertificate(c)));
                stationStore.setStationCertificateChain(stationCertificateChain);
            }
        } else {
            stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedResponse.Status.REJECTED));
        }
    }

    private void startCertificateRenewerTask(X509Certificate certificate) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        long expiration = certificate.getNotAfter().toInstant().getEpochSecond();
        long now = Instant.now().getEpochSecond();
        long waitTime = Math.max((expiration - now), 0);
        scheduledFuture = EXECUTOR.schedule(new SignCertificateRequestHandler(stationStore, stationMessageSender), waitTime, TimeUnit.SECONDS);
    }

    ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }

    private X509Certificate convertStringToCertificate(String cert) {
        try {
            byte[] bytes = Hex.decode(cert);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(bytes);
            return (X509Certificate) factory.generateCertificate(in);
        } catch (Exception e) {
            log.debug("Invalid certificate", e);
        }
        return null;
    }

    private boolean isCertificateValid(X509Certificate certificate) {
        try {
            certificate.checkValidity();
            String serialCode = StringUtils.removeStart(certificate.getSubjectDN().getName(), "CN=");
            if (!StationHardwareData.SERIAL_NUMBER.equals(serialCode)) {
                return false;
            }
        } catch (Exception e) {
            log.debug("Exception while checking certificate validity", e);
            return false;
        }
        return true;
    }
}
