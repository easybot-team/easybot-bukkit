package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import dev.unnm3d.redischat.api.events.AsyncRedisChatMessageEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RedisChatMessageSyncEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public static void syncMessage(AsyncRedisChatMessageEvent event){
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_chat")) return;
        if(!event.isCancelled()){
            if(event.getSender() instanceof Player){
                Player player = (Player) event.getSender();
                PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(player);
                String message = PlainTextComponentSerializer.plainText().serialize(event.getContent());
                new Thread(() -> Easybot.getClient().syncMessage(playerInfo, message, false), "EasyBotThread-SyncMessage(PlayerChat)").start();
            }
        }
    }
}
