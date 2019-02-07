package com.evbox.everon.ocpp.simulator.station;

@FunctionalInterface
interface Subscriber<REQ, RES> {
    void onResponse(REQ request, RES response);
}