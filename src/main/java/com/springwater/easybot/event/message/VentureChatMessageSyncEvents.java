package com.springwater.easybot.event.message;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VentureChatMessageSyncEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public static void syncMessage(VentureChatEvent event) {
        if (Easybot.instance.getConfig().getBoolean("skip_options.skip_chat")) return;
        MineverseChatPlayer mcp = event.getMineverseChatPlayer();
        if (mcp.getPlayer() == null) return;
        if (FakePlayerUtils.isFake(mcp.getPlayer())) return;
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(mcp.getPlayer());
        Easybot.EXECUTOR.execute(() -> Easybot.getClient().syncMessage(playerInfo, event.getChat(), false));
    }
}
