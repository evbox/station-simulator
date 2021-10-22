package com.evbox.everon.ocpp.simulator.configuration;

import com.evbox.everon.ocpp.simulator.station.StationHardwareData;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
public class SimulatorConfiguration {

    private static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    private static final int DEFAULT_EV_CONNECTION_TIMEOUT = 60;
    private static final List<String> DEFAULT_TX_START_POINTS = Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED.toString(), TxStartStopPointVariableValues.EV_CONNECTED.toString());
    private static final List<String> DEFAULT_TX_STOP_POINTS = Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED.toString(), TxStartStopPointVariableValues.EV_CONNECTED.toString());

    private static final long DEFAULT_SEND_METER_VALUES_INTERVAL_SEC = 10;
    private static final long DEFAULT_CONSUMPTION_WATT_HOUR = 22_000;

    private static final long DEFAULT_CALL_TIMEOUT = 10_000;
    private static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
    private static final long DEFAULT_READ_TIMEOUT = 10_000;
    private static final long DEFAULT_WRITE_TIMEOUT = 10_000;

    private static final long DEFAULT_PING_INTERVAL = 30_000;

    private static final ConnectorStatus DEFAULT_EVSE_STATUS = ConnectorStatus.AVAILABLE;

    private WebSocketConfiguration socketConfiguration;
    private List<StationConfiguration> stations;

    @Data
    public static class StationConfiguration {
        private String id;
        private Evse evse;
        private MeterValuesConfiguration meterValuesConfiguration = MeterValuesConfiguration.builder().build();
        private ComponentsConfiguration componentsConfiguration = ComponentsConfiguration.builder().build();
        private HardwareConfiguration hardwareConfiguration = HardwareConfiguration.builder().build();
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

        /**
         * Status of connector per each EVSE
         */
        private ConnectorStatus status = DEFAULT_EVSE_STATUS;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterValuesConfiguration {
        /**
         * How often send meter values in seconds.
         * If set to 0 then meter values are never sent.
         */
        @Builder.Default
        private long sendMeterValuesIntervalSec = DEFAULT_SEND_METER_VALUES_INTERVAL_SEC;

        /**
         * Power consumption
         */
        @Builder.Default
        private long consumptionWattHour = DEFAULT_CONSUMPTION_WATT_HOUR;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentsConfiguration {
        /**
         * Security controller component
         */
        @Builder.Default
        private SecurityComponentConfiguration securityCtrlr = SecurityComponentConfiguration.builder().build();

        /**
         * Transactions controller component
         */
        @Builder.Default
        private TransactionComponentConfiguration txCtrlr = TransactionComponentConfiguration.builder().build();

        /**
         * Authorisation controller componenet
         */
        @Builder.Default
        private AuthComponentConfiguration authCtrl = AuthComponentConfiguration.builder().build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityComponentConfiguration {
        /**
         * Authentication password used for HTTP basic authentication
         */
        private String basicAuthPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthComponentConfiguration {
        /**
         * Station transaction authorisation toggle
         */
        @Builder.Default
        private boolean authEnabled = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionComponentConfiguration {
        /**
         * Interval timeout in seconds between the start of the transaction
         * an the plugging of the cable
         */
        @Builder.Default
        private int evConnectionTimeOutSec = DEFAULT_EV_CONNECTION_TIMEOUT;

        /**
         * List of events that defines when a new transaction should start.
         */
        @Builder.Default
        private List<String> txStartPoints = DEFAULT_TX_START_POINTS;

        /**
         * List of events that when no longer valid, the transaction should be ended.
         */
        @Builder.Default
        private List<String> txStopPoints = DEFAULT_TX_STOP_POINTS;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HardwareConfiguration {
        /**
         * Station Vendor
         */
        @Builder.Default
        private String vendor = StationHardwareData.VENDOR_NAME;

        /**
         * Station Model
         */
        @Builder.Default
        private String model = StationHardwareData.MODEL;
    }
}
