package com.evbox.everon.ocpp.simulator.station.support.security;

import java.util.Optional;

public interface UnsafeSupplier<T> {
    T get() throws Exception;

    static <T> Optional<T> getSafe(UnsafeSupplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
