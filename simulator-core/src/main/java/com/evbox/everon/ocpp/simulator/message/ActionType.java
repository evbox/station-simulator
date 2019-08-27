package com.evbox.everon.ocpp.simulator.message;

import com.evbox.everon.ocpp.simulator.station.exceptions.UnknownActionException;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.evbox.everon.ocpp.v20.message.station.*;

import java.util.Arrays;

public enum ActionType {

    BOOT_NOTIFICATION("BootNotification", BootNotificationRequest.class, BootNotificationResponse.class),
    HEARTBEAT("Heartbeat", HeartbeatRequest.class, HeartbeatResponse.class),
    AUTHORIZE("Authorize", AuthorizeRequest .class, AuthorizeResponse .class),
    RESET("Reset", ResetRequest.class, ResetResponse.class),
    TRANSACTION_EVENT("TransactionEvent", TransactionEventRequest.class, TransactionEventResponse.class),
    STATUS_NOTIFICATION("StatusNotification", StatusNotificationRequest.class, StatusNotificationResponse.class),
    GET_VARIABLES("GetVariables", GetVariablesRequest.class, GetVariablesResponse.class),
    SET_VARIABLES("SetVariables", SetVariablesRequest.class, SetVariablesResponse.class),
    CHANGE_AVAILABILITY("ChangeAvailability", ChangeAvailabilityRequest.class, ChangeAvailabilityResponse.class),
    NOTIFY_REPORT("NotifyReport", NotifyReportRequest.class, NotifyReportResponse.class),
    GET_BASE_REPORT("GetBaseReport", GetBaseReportRequest.class, GetBaseReportResponse.class),
    REQUEST_STOP_TRANSACTION("RequestStopTransaction", RequestStopTransactionRequest.class, RequestStopTransactionResponse.class);

    private final String actionType;
    private final Class requestClazz;
    private final Class responseClazz;

    ActionType(String actionType, Class requestClazz, Class responseClazz) {
        this.actionType = actionType;
        this.requestClazz = requestClazz;
        this.responseClazz = responseClazz;
    }

    public String getType() {
        return actionType;
    }

    public Class getRequestType() {
        return requestClazz;
    }

    public Class getResponseType() {
        return responseClazz;
    }

    public static ActionType of(String actionType) {
        return Arrays.stream(values())
                .filter(type -> type.getType().equals(actionType))
                .findAny()
                .orElseThrow(() -> new UnknownActionException("Incorrect action type: " + actionType));
    }
}
