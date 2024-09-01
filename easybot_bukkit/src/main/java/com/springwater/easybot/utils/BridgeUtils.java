package com.springwater.easybot.utils;

import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import org.bukkit.entity.Player;

public class BridgeUtils {
    public static PlayerInfoWithRaw buildPlayerInfoFull(Player player){
        PlayerInfoWithRaw playerInfoWithRaw = new PlayerInfoWithRaw();
        playerInfoWithRaw.setName(player.getName());
        playerInfoWithRaw.setNameRaw(player.getName());
        playerInfoWithRaw.setUuid(player.getUniqueId().toString());
        playerInfoWithRaw.setIp(player.getAddress().getHostName());
        return playerInfoWithRaw;
    }

    public static PlayerInfo buildPlayerInfo(Player player){
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerName(player.getName());
        playerInfo.setPlayerUuid(player.getUniqueId().toString());
        playerInfo.setIp(player.getAddress().getHostName());
        return playerInfo;
    }
}
