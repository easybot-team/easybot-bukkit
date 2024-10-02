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
        playerInfoWithRaw.setIp(getIp(player));
        return playerInfoWithRaw;
    }

    public static String getIp(Player player){
        return "";
        /*
        try{
            return player.getAddress().getHostName();
        }catch (Exception e){
            return "127.0.0.1";
        }
         */
    }

    public static PlayerInfo buildPlayerInfo(Player player){
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerName(player.getName());
        playerInfo.setPlayerUuid(player.getUniqueId().toString());
        playerInfo.setIp(getIp(player));
        return playerInfo;
    }
}
