package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TxStartStopPointVariableValues {

    EV_CONNECTED("EVConnected"),
    AUTHORIZED("Authorized"),
    DATA_SIGNED("DataSigned"),
    POWER_PATH_CLOSED("PowerPathClosed"),
    ENERGY_TRANSFER("EnergyTransfer");

    private final String value;
    private static final Map<String, TxStartStopPointVariableValues> CONSTANTS = new HashMap<>();

    static {
        for (TxStartStopPointVariableValues c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }
    TxStartStopPointVariableValues(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static TxStartStopPointVariableValues fromValue(String value) {
        TxStartStopPointVariableValues constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static List<TxStartStopPointVariableValues> fromValues(List<String> values) {
        List<TxStartStopPointVariableValues> result = new ArrayList<>();
        for (String value : values) {
            if (CONSTANTS.get(value) != null) {
                result.add(CONSTANTS.get(value));
            }
        }
        return result;
    }

    public static boolean validateStringOfValues(String txPoints) {
        String[] values = txPoints.split(",");
        for (String value : values) {
            if (CONSTANTS.get(value) == null) {
                return false;
            }
        }
        return true;
    }
}
