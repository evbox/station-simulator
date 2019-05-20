package com.evbox.everon.ocpp.simulator.station.support;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class HexUtilsTest {

    @ParameterizedTest
    @ValueSource(chars = {'a', 'b', 'c', 'd', 'e', 'f'})
    void validLowerCaseChar(char ch) {

        assertThat(HexUtils.isNotLowerCaseChar(ch)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(chars = {'A', 'B', 'C', 'D', 'E', 'F'})
    void validUpperCaseChar(char ch) {

        assertThat(HexUtils.isNotUpperCaseChar(ch)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'})
    void validDigit(char ch) {

        assertThat(HexUtils.isNotDigit(ch)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0123456789", "abcdef", "ABCDEF", "0123456789abcdefABCDEF"})
    void validHex(String str) {

        assertThat(HexUtils.isNotHex(str)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"gijklmnoprstvqwyxz", "~!@#$%^&*()_+"})
    void invalidHex(String str) {

        assertThat(HexUtils.isNotHex(str)).isTrue();
    }
}
