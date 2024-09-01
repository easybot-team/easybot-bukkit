package com.springwater.easybot.utils;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;

public class BukkitUtils {
    public static String tryGetServerDescription() {
        try {
            return Bukkit.getName() + " " + Bukkit.getBukkitVersion();
        } catch (Exception ignored) {
            return "%过于冷门的服务端%";
        }
    }

    public static boolean placeholderApiInstalled() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static boolean isFolia() {
        try {
            Bukkit.class.getMethod("getRegionScheduler");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean isPaperMessageEvent() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
