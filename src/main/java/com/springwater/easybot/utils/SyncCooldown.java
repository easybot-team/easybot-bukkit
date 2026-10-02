package com.springwater.easybot.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/** Each instance tracks one event type. Suppressed events do not extend the cooldown. */
public final class SyncCooldown {
    private int seconds;
    private Cache<String, Boolean> sent;

    public synchronized boolean allow(String key, int cooldownSeconds) {
        if (seconds != cooldownSeconds) {
            seconds = cooldownSeconds;
            sent = seconds > 0
                    ? CacheBuilder.newBuilder().expireAfterWrite(seconds, TimeUnit.SECONDS).build()
                    : null;
        }
        return sent == null || sent.asMap().putIfAbsent(key, Boolean.TRUE) == null;
    }
}
