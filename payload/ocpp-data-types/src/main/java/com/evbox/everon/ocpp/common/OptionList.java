package com.evbox.everon.ocpp.common;

import java.util.*;

public class OptionList<T> implements Iterable<T> {

    private final Set<T> set;

    public OptionList(List<T> values) {
        set = new HashSet<>(values);
    }

    public boolean contains(T value) {
        return set.contains(value);
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (T e : set) {
            joiner.add(e.toString());
        }
        return joiner.toString();
    }
}
