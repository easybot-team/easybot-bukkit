package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class BukkitSideMessageSyncEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public static void syncMessage(AsyncPlayerChatEvent event){
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_chat")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        if(!event.isCancelled()){
            PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
            Easybot.EXECUTOR.execute(() -> Easybot.getClient().syncMessage(playerInfo, event.getMessage(), false));
        }
    }
}