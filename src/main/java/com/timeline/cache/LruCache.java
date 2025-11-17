package com.timeline.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<K, V> {
    private final int maxSize;
    private final long ttlMillis;
    private final Map<K, Entry<V>> map;

    public LruCache(int maxSize, long ttlMillis) {
        this.maxSize = maxSize;
        this.ttlMillis = ttlMillis;
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, Entry<V>> eldest) {
                return size() > LruCache.this.maxSize;
            }
        };
    }

    public synchronized V get(K key) {
        Entry<V> e = map.get(key);
        if (e == null) return null;
        if ((System.currentTimeMillis() - e.ts) > ttlMillis) {
            map.remove(key);
            return null;
        }
        return e.val;
    }

    public synchronized void put(K key, V value) {
        map.put(key, new Entry<>(value));
    }

    public synchronized void invalidate(K key) {
        map.remove(key);
    }

    public synchronized void clear() {
        map.clear();
    }

    private static class Entry<V> {
        final V val;
        final long ts;
        Entry(V v) { this.val = v; this.ts = System.currentTimeMillis(); }
    }
}