package com.app.util;

public class ImageShardContext {

    private static final ThreadLocal<String> HASH_PREFIX = new ThreadLocal<>();

    public static void setHashPrefix(String hash) {
        if (hash != null && hash.length() >= 2) {
            HASH_PREFIX.set(hash.substring(0, 2).toLowerCase());
        }
    }

    public static String getHashPrefix() {
        return HASH_PREFIX.get();
    }

    public static void clear() {
        HASH_PREFIX.remove();
    }
}
