package com.evbox.everon.ocpp.common;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class CiString implements Comparable<CiString> {
    private static final Predicate<String> ASCII = Pattern.compile("^\\p{ASCII}*$").asPredicate();

    private final String string;
    private final String lowerCaseString;

    private CiString(String string, int maxLength) {
        checkArgument(string != null);
        checkArgument(string.length() <= maxLength);
        checkArgument(ASCII.test(string));

        this.string = string;
        this.lowerCaseString = string.toLowerCase();
    }

    @Override
    @JsonValue
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CiString ciString = (CiString) o;
        return Objects.equals(lowerCaseString, ciString.lowerCaseString);
    }

    public boolean equalsIgnoreCase(String s) {
        return string.equalsIgnoreCase(s);
    }

    @Override
    public int hashCode() {
        return lowerCaseString.hashCode();
    }

    @Override
    public int compareTo(CiString o) {
        return lowerCaseString.compareTo(o.lowerCaseString);
    }

    public static class CiString6 extends CiString {
        public CiString6(String string) {
            super(string, 6);
        }
    }

    public static class CiString8 extends CiString {
        public CiString8(String string) {
            super(string, 8);
        }
    }

    public static class CiString20 extends CiString {
        public CiString20(String string) {
            super(string, 20);
        }
    }

    public static class CiString25 extends CiString {
        public CiString25(String string) {
            super(string, 25);
        }
    }

    public static class CiString32 extends CiString {
        public CiString32(String string) {
            super(string, 32);
        }
    }

    public static class CiString36 extends CiString {
        public CiString36(String string) {
            super(string, 36);
        }
    }

    public static class CiString50 extends CiString {
        public CiString50(String string) {
            super(string, 50);
        }
    }

    public static class CiString64 extends CiString {
        public CiString64(String string) {
            super(string, 64);
        }
    }

    public static class CiString128 extends CiString {
        public CiString128(String string) {
            super(string, 128);
        }
    }

    public static class CiString255 extends CiString {
        public CiString255(String string) {
            super(string, 255);
        }
    }

    public static class CiString500 extends CiString {
        public CiString500(String string) {
            super(string, 500);
        }
    }

    public static class CiString512 extends CiString {
        public CiString512(String string) { super(string, 512); }
    }

    public static class CiString800 extends CiString {
        public CiString800(String string) { super(string, 800); }
    }

    public static class CiString1000 extends CiString {
        public CiString1000(String string) {
            super(string, 1000);
        }
    }

    public static class CiString2500 extends CiString {
        public CiString2500(String string) {
            super(string, 2500);
        }
    }

    public static class CiString5500 extends CiString {
        public CiString5500(String string) {
            super(string, 5500);
        }
    }

    public static class IdToken extends CiString20 {
        public IdToken(String string) {
            super(string);
        }
    }

    public static Class<? extends CiString> type(String field, int maxLength) {
        switch (maxLength) {
            case 8:
                return CiString8.class;
            case 20:
                return field.toLowerCase().endsWith("idtag") ? IdToken.class : CiString20.class;
            case 25:
                return CiString25.class;
            case 36:
                return CiString36.class;
            case 50:
                return CiString50.class;
            case 128:
                return CiString128.class;
            case 255:
                return CiString255.class;
            case 500:
                return CiString500.class;
            case 512:
                return CiString512.class;
            case 1000:
                return CiString1000.class;
            case 2500:
                return CiString2500.class;
            default:
                throw new IllegalArgumentException("OCPP specification does not allow CiString with maximum length " + maxLength);
        }
    }

    public static class CiStringSizeValidator implements ConstraintValidator<Size, CiString> {
        private int min;
        private int max;

        @Override
        public void initialize(Size parameters) {
            checkArgument(parameters.min() >= 0);
            checkArgument(parameters.max() >= 0 && parameters.max() >= parameters.min());
            min = parameters.min();
            max = parameters.max();
        }

        @Override
        public boolean isValid(CiString value, ConstraintValidatorContext context) {
            int valueSize = Optional.ofNullable(value).map(CiString::toString).map(String::length).orElse(0);
            return valueSize>= min && valueSize <= max;
        }
    }
}
