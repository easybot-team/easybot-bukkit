package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperSideMessageSyncEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public static void syncMessage(AsyncChatEvent event){
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_chat")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        if(!event.isCancelled()){
            PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            Easybot.EXECUTOR.execute(() -> Easybot.getClient().syncMessage(playerInfo,message , false));
        }
    }
}