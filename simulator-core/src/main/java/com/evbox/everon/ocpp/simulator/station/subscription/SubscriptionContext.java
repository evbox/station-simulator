package com.evbox.everon.ocpp.simulator.station.subscription;

import lombok.Value;

@Value
public class SubscriptionContext<R, S> {
    Subscriber<R, S> subscriber;
    R request;
}
