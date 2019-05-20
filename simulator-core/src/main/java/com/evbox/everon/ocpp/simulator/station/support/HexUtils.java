package com.evbox.everon.ocpp.simulator.station.support;

public class HexUtils {

    public static boolean isNotHex(String value) {

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (isNotLowerCaseChar(ch) && isNotUpperCaseChar(ch) && isNotDigit(ch)) {
                return true;
            }
        }

        return false;
    }

    static boolean isNotLowerCaseChar(char ch) {
        return ch < 'a' || 'f' < ch;
    }


    static boolean isNotUpperCaseChar(char ch) {
        return ch < 'A' || 'F' < ch;
    }

    static boolean isNotDigit(char ch) {
        return ch < '0' || '9' < ch;
    }
}
