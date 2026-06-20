package com.app.cache;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VendorConfirmCache {

    private final Map<String, List<Long>> cache = new ConcurrentHashMap<>();

    public String put(List<Long> sampleIds) {
        String key = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        cache.put(key, sampleIds);
        return key;
    }

    public List<Long> get(String key) {
        return cache.get(key);
    }
}
