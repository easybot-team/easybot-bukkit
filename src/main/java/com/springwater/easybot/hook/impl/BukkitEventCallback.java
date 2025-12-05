package com.springwater.easybot.hook.impl;

import org.bukkit.Server;
import org.bukkit.event.Event;

import java.util.function.BiPredicate;

public class BukkitEventCallback {
    private static BukkitEventCallback instance;
    public static BukkitEventCallback getInstance() {
        return instance;
    }

    private final BiPredicate<Server, Event> callback;

    public BukkitEventCallback(BiPredicate<Server, Event> callback) {
        instance = this;
        this.callback = callback;
    }

    public boolean onCallEvent(Server server, Event event) {
        return callback.test(server, event);
    }
}