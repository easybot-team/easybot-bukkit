package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperSideMessageSyncEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public static void syncMessage(AsyncChatEvent event){
        if(!event.isCancelled()){
            PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            new Thread(() -> Easybot.getClient().syncMessage(playerInfo,message , false), "EasyBotThread-SyncMessage(PaperSide)").start();
        }
    }
}