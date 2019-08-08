package com.evbox.everon.ocpp.simulator.configuration;

import lombok.Data;

import java.util.List;

@Data
public class SimulatorConfiguration {

    private static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    private WebSocketConfiguration socketConfiguration;
    private List<StationConfiguration> stations;

    @Data
    public static class StationConfiguration {
        private String id;
        private Evse evse;
        private String basicAuthPassword;
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
    public static class WebSocketConfiguration {
        /**
         * Call timeout in milliseconds
         */
        private Long callTimeout;

        /**
         * Connection timeout in milliseconds
         */
        private Long connectTimeout;

        /**
         * Read timeout in milliseconds
         */
        private Long readTimeout;

        /**
         * Write timeout in milliseconds
         */
        private Long writeTimeout;
    }
}