package com.evbox.everon.ocpp.simulator.message;

import lombok.Value;

import java.util.Arrays;

@Value
public class CallError {

    private RawCall rawCall;

    public CallError(RawCall rawCall) {
        this.rawCall = rawCall;
    }

    public CallError(String callId, Code errorCode, Object payload) {
        this(new RawCall(Arrays.asList(MessageType.CALL_ERROR.getId(), callId, errorCode.val, errorCode.val, payload)));
    }

    public String toJson() {
        return rawCall.toJson();
    }

    public enum Code {
        FORMAT_VIOLATION("FormatViolation"),
        FORMATION_VIOLATION("FormationViolation"),
        GENERIC_ERROR("GenericError"),
        INTERNAL_ERROR("InternalError"),
        MESSAGE_TYPE_NOT_SUPPORTED("MessageTypeNotSupported"),
        NOT_IMPLEMENTED("NotImplemented"),
        NOT_SUPPORTED("NotSupported"),
        OCCURRENCE_CONSTRAINT_VIOLATION("OccurrenceConstraintViolation"),
        PROPERTY_CONSTRAINT_VIOLATION("PropertyConstraintViolation"),
        PROTOCOL_ERROR("ProtocolError"),
        RPC_FRAMEWORK_ERROR("RpcFrameworkError"),
        SECURITY_ERROR("SecurityError"),
        TYPE_CONSTRAINT_VIOLATION("TypeConstraintViolation");

        private final String val;

        Code(String val) {
            this.val = val;
        }

        public static Code fromString(String val) {
            if (val == null) {
                return null;
            }
            return Arrays.stream(values()).filter(item -> item.val.equals(val)).findFirst().orElse(PROTOCOL_ERROR);
        }
    }
}
