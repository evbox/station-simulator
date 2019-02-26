package com.evbox.everon.ocpp.simulator.assertion;

import com.evbox.everon.ocpp.common.CiString;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class CiStringAssert extends AbstractAssert<CiStringAssert, CiString> {

    public CiStringAssert(CiString actual) {
        super(actual, CiStringAssert.class);
    }

    public static CiStringAssert assertCiString(CiString actual) {
        return new CiStringAssert(actual);
    }

    public CiStringAssert isEqualTo(String expectedStr) {
        isNotNull();

        String actualString = actual.toString();
        if (!Objects.equals(actualString, expectedStr)) {
            failWithMessage("Expected string to be <%s> but was <%s>", expectedStr, actualString);
        }

        return this;
    }
}
