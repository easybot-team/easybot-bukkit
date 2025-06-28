package com.springwater.easybot.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class BukkitUtils {
    public static String tryGetServerDescription() {
        try {
            return Bukkit.getName() + " " + Bukkit.getBukkitVersion();
        } catch (Exception ignored) {
            return "%过于冷门的服务端%";
        }
    }

    public static boolean canCreateCommandSender() {
        try {
            // 找 public static @NotNull CommandSender createCommandSender(@NotNull Consumer<? super Component> feedback)
            Class.forName("org.bukkit.Bukkit")
                    .getMethod("createCommandSender", Consumer.class);
            return true;
        } catch (Exception e) {
            return false;
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

    public static boolean hasPlayerChatPlugin() {
        try {
            Class.forName("cn.handyplus.chat.event.PlayerChannelChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean canUsePlayerChatEvent() {
        try {
            Class.forName("cn.handyplus.chat.event.PlayerChannelChatEvent").getMethod("getOriginalMessage");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasRedisChatPlugin() {
        try {
            Class.forName("dev.unnm3d.redischat.api.events.AsyncRedisChatMessageEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isSupportStatistic() {
        try {
            Method[] methods = Class.forName("org.bukkit.OfflinePlayer").getMethods();
            for (Method method : methods) {
                if (method.getName().equals("getStatistic"))
                    return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasGeyserMc() {
        try {
            Class.forName("org.geysermc.api.BuildData");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasFloodgate() {
        try {
            Class.forName("org.geysermc.floodgate.api.player.FloodgatePlayer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasBungeeChatApi() {
        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasSkinsRestorer() {
        try {
            Class.forName("net.skinsrestorer.api.SkinsRestorerProvider");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasPaperSkinApi() {
        try {

            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            Class.forName("org.bukkit.profile.PlayerTextures");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
