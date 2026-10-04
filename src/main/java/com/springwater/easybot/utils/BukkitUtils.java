package com.springwater.easybot.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class BukkitUtils {

    private static final Method IS_OWNED_BY_CURRENT_REGION_WORLD_CHUNK =
            lookupBukkitMethod("isOwnedByCurrentRegion", World.class, int.class, int.class);
    private static final Method IS_OWNED_BY_CURRENT_REGION_LOCATION =
            lookupBukkitMethod("isOwnedByCurrentRegion", Location.class);

    private static Method lookupBukkitMethod(String name, Class<?>... parameterTypes) {
        try {
            return Bukkit.class.getMethod(name, parameterTypes);
        } catch (Throwable ignored) {
            return null;
        }
    }
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

    /**
     * Folia 专属判断：目标区块是否由「当前区域线程」拥有。
     * <p>
     * 非 Folia 服务端（或该 API 不可用）返回 {@code true}，表示没有区域归属限制。
     * 本方法只查询归属，<b>不会</b>加载区块，可以在事件回调里安全调用。
     */
    public static boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ) {
        Method byChunk = IS_OWNED_BY_CURRENT_REGION_WORLD_CHUNK;
        if (byChunk != null) {
            try {
                return Boolean.TRUE.equals(byChunk.invoke(null, world, chunkX, chunkZ));
            } catch (Throwable ignored) {
                return false;
            }
        }
        Method byLocation = IS_OWNED_BY_CURRENT_REGION_LOCATION;
        if (byLocation != null) {
            try {
                return Boolean.TRUE.equals(byLocation.invoke(null, new Location(world, chunkX << 4, 0, chunkZ << 4)));
            } catch (Throwable ignored) {
                return false;
            }
        }
        return true;
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
    
    public static boolean hasVentureChat() {
        try {
            Class.forName("mineverse.Aust1n46.chat.MineverseChat");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
