package com.evbox.everon.ocpp.simulator.station.support.security;

import java.util.Optional;

public interface ThrowingSupplier<T> {
    T get() throws Exception;

    static <T> Optional<T> getAndSuppressException(ThrowingSupplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
