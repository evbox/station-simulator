package com.evbox.everon.ocpp.simulator.station;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

@Slf4j
class NullHostNameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        log.info("Approving certificate for {}", hostname);
        return true;
    }
}