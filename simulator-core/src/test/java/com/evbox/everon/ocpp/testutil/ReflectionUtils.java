package com.evbox.everon.ocpp.testutil;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReflectionUtils {


    /**
     * Inject mock to the the supplied fieldName on the specified {@link Object target object}.
     * Does not scan superClass.
     *
     * @param target    the target object from which to get the field
     * @param fieldName the field name in the target object
     * @param mock      mock to be injected in the supplied field
     */

    public static void injectMock(Object target, String fieldName, Object mock) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(mock);

        try {
            Field field = findField(target.getClass(), fieldName, mock.getClass());

            field.setAccessible(true);
            field.set(target, mock);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(
                    "Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private static Field findField(Class<?> clazz, String name, Class<?> mockType) {

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            if (name.equals(field.getName()) && field.getType().isAssignableFrom(mockType)) {
                return field;
            }
        }

        throw new RuntimeException("Could not find field with the name: " + name);
    }
}
