package com.evbox.everon.ocpp.simulator.station;

import lombok.Value;

import java.util.concurrent.atomic.AtomicInteger;

@Value
public class TransactionSequence {
    private AtomicInteger transactionIdSeq = new AtomicInteger(1);

    public Integer getNext() {
        return transactionIdSeq.getAndIncrement();
    }
}
