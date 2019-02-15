package com.evbox.everon.ocpp.simulator.station.subscription;

@FunctionalInterface
public interface Subscriber<REQ, RES> {
    void onResponse(REQ request, RES response);
}