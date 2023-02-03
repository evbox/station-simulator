package com.evbox.everon.ocpp.simulator.message;

import com.evbox.everon.ocpp.simulator.station.exceptions.UnknownActionException;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.*;

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
    REQUEST_STOP_TRANSACTION("RequestStopTransaction", RequestStopTransactionRequest.class, RequestStopTransactionResponse.class),
    REQUEST_START_TRANSACTION("RequestStartTransaction", RequestStartTransactionRequest.class, RequestStartTransactionResponse.class),
    SET_CHARGING_PROFILE("SetChargingProfile", SetChargingProfileRequest.class, SetChargingProfileResponse.class),
    UNLOCK_CONNECTOR("UnlockConnector", UnlockConnectorRequest.class, UnlockConnectorResponse.class),
    SIGN_CERTIFICATE("SignCertificate", SignCertificateRequest.class, SignCertificateResponse.class),
    TRIGGER_MESSAGE("TriggerMessage", TriggerMessageRequest.class, TriggerMessageResponse.class),
    SET_VARIABLE_MONITORING("SetVariableMonitoring", SetVariableMonitoringRequest.class, SetVariableMonitoringResponse.class),
    CLEAR_VARIABLE_MONITORING("ClearVariableMonitoring", ClearVariableMonitoringRequest.class, ClearVariableMonitoringResponse.class),
    GET_MONITORING_REPORT("GetMonitoringReport", GetMonitoringReportRequest.class, GetMonitoringReportResponse.class),
    NOTIFY_MONITORING_REPORT("NotifyMonitoringReport", NotifyMonitoringReportRequest.class, NotifyMonitoringReportResponse.class),
    CERTIFICATE_SIGNED("CertificateSigned", CertificateSignedRequest.class, CertificateSignedResponse.class),
    SEND_LOCAL_LIST("SendLocalList", SendLocalListRequest.class, SendLocalListResponse.class),
    SET_NETWORK_PROFILE("SetNetworkProfile", SetNetworkProfileRequest.class, SetNetworkProfileResponse.class),
    RESERVE_NOW("ReserveNow", ReserveNowRequest.class, ReserveNowResponse.class),
    NOTIFY_EVENT("NotifyEvent", NotifyEventRequest.class, NotifyEventResponse.class),
    CUSTOMER_INFORMATION("CustomerInformation", CustomerInformationRequest.class, CustomerInformationResponse.class),
    NOTIFY_CUSTOMER_INFORMATION("NotifyCustomerInformation", NotifyCustomerInformationRequest.class, NotifyCustomerInformationResponse.class),
    CANCEL_RESERVATION("CancelReservation", CancelReservationRequest.class, CancelReservationResponse.class),
    DATA_TRANSFER("DataTransfer", DataTransferRequest.class, DataTransferResponse.class),
    INSTALL_CERTIFICATE("InstallCertificate", InstallCertificateRequest.class, InstallCertificateResponse.class),
    NOTIFY_EV_CHARGING_NEEDS("NotifyEVChargingNeeds", NotifyEVChargingNeedsRequest.class, NotifyEVChargingNeedsResponse.class);

    private final String actionTypeName;
    private final Class requestClazz;
    private final Class responseClazz;

    ActionType(String actionTypeName, Class requestClazz, Class responseClazz) {
        this.actionTypeName = actionTypeName;
        this.requestClazz = requestClazz;
        this.responseClazz = responseClazz;
    }

    public String getType() {
        return actionTypeName;
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
