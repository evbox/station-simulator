package com.evbox.everon.ocpp.simulator.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class SimulatorConfiguration {

    private static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    private static final long DEFAULT_METER_VALUES_INTERVAL = 1_000;
    private static final long DEFAULT_POWER_CONSUMPTION_PER_INTERVAL = 100;

    private static final long DEFAULT_CALL_TIMEOUT = 10_000;
    private static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
    private static final long DEFAULT_READ_TIMEOUT = 10_000;
    private static final long DEFAULT_WRITE_TIMEOUT = 10_000;

    private static final long DEFAULT_PING_INTERVAL = 10_000;

    private WebSocketConfiguration socketConfiguration;
    private List<StationConfiguration> stations;

    @Data
    public static class StationConfiguration {
        private String id;
        private Evse evse;
        private String basicAuthPassword;
        private MeterValuesConfiguration meterValuesConfiguration;
    }

    @Data
    public static class Evse {
        /**
         * Amount of EVSEs for current station
         */
        private int count;

        /**
         * Amount of connectors per each EVSE
         */
        private int connectors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterValuesConfiguration {
        /**
         * How often send meter values in milliseconds
         */
        @Builder.Default
        private long meterValuesIntervalMs = DEFAULT_METER_VALUES_INTERVAL;

        /**
         * Power consumed for each power consumption interval
         */
        @Builder.Default
        private long powerConsumptionPerInterval = DEFAULT_POWER_CONSUMPTION_PER_INTERVAL;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebSocketConfiguration {
        /**
         * Call timeout in milliseconds
         */
        @Builder.Default
        private long callTimeoutMs = DEFAULT_CALL_TIMEOUT;

        /**
         * Connection timeout in milliseconds
         */
        @Builder.Default
        private long connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT;

        /**
         * Read timeout in milliseconds
         */
        @Builder.Default
        private long readTimeoutMs = DEFAULT_READ_TIMEOUT;

        /**
         * Write timeout in milliseconds
         */
        @Builder.Default
        private long writeTimeoutMs = DEFAULT_WRITE_TIMEOUT;

        /**
         * Ping interval in milliseconds
         */
        @Builder.Default
        private long pingIntervalMs = DEFAULT_PING_INTERVAL;
    }
}