package com.evbox.everon.ocpp.simulator.support;

import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;

public class StationConstants {

    public static final String HOST = "localhost";
    public static final int PORT = 8085;
    public static final String PATH = "/ocpp";
    public static final String OCPP_SERVER_URL = "ws://" + HOST + ":" + PORT + PATH;

    public static final String STATION_ID = "EVB-P18090564";

    public static final int DEFAULT_EVSE_COUNT = 1;

    public static final int DEFAULT_EVSE_CONNECTORS = 1;

    public static final int EVSE_COUNT_TWO = 2;

    public static final int EVSE_CONNECTORS_TWO = 2;

    public static final String SERVER_BASE_URL = "ws://ocpp.server.io:8083/ocpp";

    public static final int DEFAULT_EVSE_ID = 1;
    public static final int EVSE_ID_ZERO = 0;
    public static final int DEFAULT_CONNECTOR_ID = 1;

    public static final String DEFAULT_TOKEN_ID = "04E8960A1A3180";

    public static final String DEFAULT_MESSAGE_ID = "1";

    public static final String DEFAULT_TRANSACTION_ID = "12345";
    public static final int DEFAULT_INT_TRANSACTION_ID = 12345;

    public static final int DEFAULT_SEQ_NUMBER = 1;

    public static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    public static final String DEFAULT_COMPONENT_NAME = "OCPPCommCtrlr";
    public static final String DEFAULT_VARIABLE_NAME = "RetryBackOffRandomRange";

    public static final Subscriber DEFAULT_SUBSCRIBER = (req, res) -> {
    };

    public static final String DEFAULT_SERIAL_NUMBER = "00000000000F";
    public static final String DEFAULT_MODEL = "G5";
    public static final String DEFAULT_FIRMWARE_VERSION = "G5-0.00.01";


    // Actions
    public static final String NOTIFY_REPORT_ACTION = "NotifyReport";
    public static final String GET_VARIABLES_ACTION = "GetVariables";
    public static final String SET_VARIABLES_ACTION = "SetVariables";
    public static final String RESET_ACTION = "Reset";
    public static final String TRANSACTION_EVENT_ACTION = "TransactionEvent";
    public static final String HEART_BEAT_ACTION = "Heartbeat";
    public static final String CHANGE_AVAILABILITY_ACTION = "ChangeAvailability";
    public static final String BOOT_NOTIFICATION_ACTION = "BootNotification";
    public static final String STATUS_NOTIFICATION_ACTION = "StatusNotification";

}
