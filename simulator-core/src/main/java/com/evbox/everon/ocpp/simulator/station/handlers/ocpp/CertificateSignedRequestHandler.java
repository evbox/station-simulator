package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationHardwareData;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v201.message.station.CertificateSignedRequest;
import com.evbox.everon.ocpp.v201.message.station.CertificateSignedResponse;
import com.evbox.everon.ocpp.v201.message.station.CertificateSignedStatus;
import com.evbox.everon.ocpp.v201.message.station.CertificateSigningUse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        if (request.getCertificateType() == CertificateSigningUse.V_2_G_CERTIFICATE) {
            stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedStatus.REJECTED));
            return;
        }

        //TODO previously in OCPP 2.0  each certificate in the chain was stored in a separate string. Verify that this still works in OCPP 2.0.1 with one signed PEM
        String chain = Optional.ofNullable(request.getCertificateChain()).map(CiString::toString).orElse("");

        if (chain.isEmpty()) {
            stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedStatus.REJECTED));
            return;
        }

        //TODO ditto: previously in OCPP 2.0  each certificate in the chain was stored in a separate string. Verify that this still works in OCPP 2.0.1 with one signed PEM
        List<X509Certificate> stationCertificates = convertStringToCertificates(chain);

        if (!stationCertificates.isEmpty()) {
            X509Certificate first = stationCertificates.get(0);
            if(isCertificateValid(first)) {
                stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedStatus.ACCEPTED));
                stationStore.setStationCertificate(first);
                startCertificateRenewerTask(first);

                if (stationCertificates.size() > 1) {
                    List<X509Certificate> stationCertificateChain = new ArrayList<>();
                    stationCertificates.subList(1, stationCertificates.size()).forEach(c -> stationCertificateChain.add(c));
                    stationStore.setStationCertificateChain(stationCertificateChain);
                }
                return;
            }
        }
        stationMessageSender.sendCallResult(callId, new CertificateSignedResponse().withStatus(CertificateSignedStatus.REJECTED));

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

    //TODO ditto: previously in OCPP 2.0  each certificate in the chain was stored in a separate string. Verify that this still works in OCPP 2.0.1 with one signed PEM
    private List<X509Certificate> convertStringToCertificates(String cert) {
        try {
            byte[] bytes = Hex.decode(cert);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(bytes);
            return factory.generateCertificates(in).stream().map(certificate -> ((X509Certificate) certificate)).collect(Collectors.toList());
        } catch (Exception e) {
            log.debug("Invalid certificate", e);
        }
        return Collections.emptyList();
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
