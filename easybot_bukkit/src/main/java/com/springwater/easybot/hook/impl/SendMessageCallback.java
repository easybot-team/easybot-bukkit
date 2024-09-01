package com.springwater.easybot.hook.impl;

import org.bukkit.Server;
import org.bukkit.event.Event;

import java.util.function.BiPredicate;

public class SendMessageCallback {
    private static SendMessageCallback instance;
    public static SendMessageCallback getInstance() {
        return instance;
    }

    private final BiPredicate<Object, Object> callback;

    public SendMessageCallback(BiPredicate<Object, Object> callback) {
        instance = this;
        this.callback = callback;
    }

    public boolean onSendMessage(Object server, Object event) {
        return callback.test(server, event);
    }
}
