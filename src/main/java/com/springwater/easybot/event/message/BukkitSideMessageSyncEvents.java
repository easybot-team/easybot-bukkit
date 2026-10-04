package com.springwater.easybot.event.message;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.ChatFilterUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class BukkitSideMessageSyncEvents implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public static void syncMessage(AsyncPlayerChatEvent event){
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_chat")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        if(!event.isCancelled()){
            PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
            String outgoing = event.getMessage();
            if (ChatFilterUtils.outgoingRejection(playerInfo, outgoing) != null) return;
            Easybot.EXECUTOR.execute(() -> Easybot.getClient().syncMessage(playerInfo, outgoing, false));
        }
    }
}