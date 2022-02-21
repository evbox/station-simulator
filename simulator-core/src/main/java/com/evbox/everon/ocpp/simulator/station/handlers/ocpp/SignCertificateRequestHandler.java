package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;


import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import lombok.extern.slf4j.Slf4j;
import java.security.*;

import static com.evbox.everon.ocpp.simulator.station.support.CertificateUtils.generateKeyPair;
import static com.evbox.everon.ocpp.simulator.station.support.CertificateUtils.generatePKCS10;

@Slf4j
public class SignCertificateRequestHandler implements Runnable {

    private StationStore stationStore;
    private StationMessageSender stationMessageSender;

    public SignCertificateRequestHandler(StationStore stationStore, StationMessageSender stationMessageSender) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
    }

    @Override
    public void run() {
        try {
            KeyPair keyPair = generateKeyPair();
            String csr = generatePKCS10(keyPair.getPublic(), keyPair.getPrivate(), stationStore.getStationSerialNumber());

            stationStore.setStationPublicKey(keyPair.getPublic());
            stationStore.setStationPrivateKey(keyPair.getPrivate());
            stationMessageSender.sendSignCertificateRequest(csr);
        } catch (Exception e) {
            log.debug("Error while creating the CSR", e);
        }

    }

}
