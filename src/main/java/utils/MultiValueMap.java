package utils;

import java.util.*;

public class MultiValueMap<K, V> {
    private final Map<K, List<V>> mappings = new HashMap<>();

    public List<V> get(K key) {
        return mappings.get(key);
    }

    public boolean put(K key, V value) {
        List<V> target = mappings.computeIfAbsent(key, k -> new ArrayList<>());
        return target.add(value);
    }

    public boolean isEmpty() {
        return mappings.isEmpty();
    }

    public boolean containsKey(Object key) {
        return mappings.containsKey(key);
    }

    @Override
    public String toString() {
        return mappings.toString();
    }
}
