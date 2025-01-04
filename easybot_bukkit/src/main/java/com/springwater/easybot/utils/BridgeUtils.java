package com.springwater.easybot.utils;

import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import org.bukkit.entity.Player;

public class BridgeUtils {
    public static PlayerInfoWithRaw buildPlayerInfoFull(Player player){
        PlayerInfoWithRaw playerInfoWithRaw = new PlayerInfoWithRaw();
        playerInfoWithRaw.setName(GeyserUtils.getName(player));
        playerInfoWithRaw.setNameRaw(GeyserUtils.getNameRaw(player));
        playerInfoWithRaw.setUuid(GeyserUtils.getUuid(player).toString());
        playerInfoWithRaw.setIp("");
        return playerInfoWithRaw;
    }
}
