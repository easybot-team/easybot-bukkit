package com.springwater.easybot.utils;

import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import org.bukkit.entity.Player;

public class BridgeUtils {
    public static PlayerInfoWithRaw buildPlayerInfoFull(Player player){
        PlayerInfoWithRaw playerInfoWithRaw = new PlayerInfoWithRaw();
        playerInfoWithRaw.setName(GeyserUtils.getNameByPlayer(player));
        playerInfoWithRaw.setNameRaw(GeyserUtils.getNameRaw(player));
        playerInfoWithRaw.setUuid(GeyserUtils.getUuid(player.getUniqueId()).toString());
        playerInfoWithRaw.setIp("");
        return playerInfoWithRaw;
    }

    public static String getPlayerIp(Player player) {
        try{
            if (player.getAddress() == null) {
                return "127.0.0.1";
            } else {
                return player.getAddress().getAddress().getHostAddress();
            }
        }catch (Exception ignored){
            return "127.0.0.1";
        }
    }
}
