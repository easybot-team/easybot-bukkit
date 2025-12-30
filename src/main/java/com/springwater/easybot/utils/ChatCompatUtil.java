package com.springwater.easybot.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.lang.reflect.Method;

public class ChatCompatUtil {

    private static boolean appendSingleComponentExists;
    private static boolean initialized = false;

    /**
     * 判断 ComponentBuilder.append(BaseComponent) 是否存在
     */
    public static boolean hasAppendMethod() {
        if (!initialized) {
            checkMethod();
        }
        return appendSingleComponentExists;
    }

    private static void checkMethod() {
        try {
            Method method = ComponentBuilder.class.getMethod("append", BaseComponent.class);
            appendSingleComponentExists = true;
        } catch (NoSuchMethodException | SecurityException e) {
            appendSingleComponentExists = false;
        }
        initialized = true;
    }

}