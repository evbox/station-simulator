package com.evbox.everon.ocpp.simulator.station.support;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private final int maxEntries;

    public LRUCache(int maxEntries) {
        super(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
    }

}
