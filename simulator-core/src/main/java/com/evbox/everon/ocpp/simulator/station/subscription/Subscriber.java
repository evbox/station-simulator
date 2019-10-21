package com.evbox.everon.ocpp.simulator.station.subscription;

@FunctionalInterface
public interface Subscriber<R, S> {
    void onResponse(R request, S response);
}