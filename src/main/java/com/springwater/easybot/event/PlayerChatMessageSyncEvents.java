package com.springwater.easybot.event;

import cn.handyplus.chat.event.PlayerChannelChatEvent;
import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerChatMessageSyncEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void syncMessage(PlayerChannelChatEvent event){
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_chat")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        if(!event.isCancelled()){
            PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
            new Thread(() -> Easybot.getClient().syncMessage(playerInfo, event.getOriginalMessage(), false), "EasyBotThread-SyncMessage(PlayerChat)").start();
        }
    }
}
