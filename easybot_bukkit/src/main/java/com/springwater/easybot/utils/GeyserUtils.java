package com.springwater.easybot.utils;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.ClientProfile;
import org.bukkit.entity.Player;
import org.geysermc.api.Geyser;
import org.geysermc.api.GeyserApiBase;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.UUID;

public class GeyserUtils {
    private static boolean toggle() {
        return Easybot.instance.getConfig().getBoolean("geyser.ignore_prefix", false);
    }

    public static String getNameRaw(Player player) {
        if (ClientProfile.isHasFloodgate() && toggle()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
            if (conn != null) {
                return conn.getJavaUsername();
            }
        }
        return player.getName();
    }

    public static String getName(Player player) {
        if (ClientProfile.isHasFloodgate()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
            if (conn != null) {
                return toggle() ? conn.getUsername() : conn.getJavaUsername();
            }
        }
        return player.getName();
    }

    public static UUID getUuid(Player player) {
        if (ClientProfile.isHasFloodgate() && toggle()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
            if (conn != null) {
                return conn.getJavaUniqueId();
            }
        }
        return player.getUniqueId();
    }

    public static boolean isBedrock(Player player) {
        if (ClientProfile.isHasGeyser()) {
            return Geyser.api().isBedrockPlayer(player.getUniqueId());
        }
        return false;
    }
}
