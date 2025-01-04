package com.springwater.easybot.utils;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;

import java.lang.reflect.Method;

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

    public static boolean hasPlayerChatPlugin(){
        try {
            Class.forName("cn.handyplus.chat.event.PlayerChannelChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean canUsePlayerChatEvent(){
        try{
            Class.forName("cn.handyplus.chat.event.PlayerChannelChatEvent").getMethod("getOriginalMessage");
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static boolean hasRedisChatPlugin(){
        try {
            Class.forName("dev.unnm3d.redischat.api.events.AsyncRedisChatMessageEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isSupportStatistic(){
        try{
            Method[] methods = Class.forName("org.bukkit.OfflinePlayer").getMethods();
            for(Method method : methods){
                if(method.getName().equals("getStatistic"))
                    return true;
            }
            return false;
        }catch(Exception e){
            return false;
        }
    }

    public static boolean hasGeyserMc(){
        try {
            Class.forName("org.geysermc.api.BuildData");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasFloodgate(){
        try {
            Class.forName("org.geysermc.floodgate.api.player.FloodgatePlayer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
