package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinExitEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_join")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
        new Thread(() -> Easybot.getClient().syncEnterExit(playerInfo, true), "EasyBotThread-SyncPlayerJoinMessage").start();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_quit")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
        new Thread(() -> Easybot.getClient().syncEnterExit(playerInfo, false), "EasyBotThread-SyncPlayerExitMessage").start();
    }
}
