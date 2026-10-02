package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import com.springwater.easybot.utils.SyncCooldown;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinExitEvents implements Listener {
    private final SyncCooldown joinCooldown = new SyncCooldown();
    private final SyncCooldown quitCooldown = new SyncCooldown();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_join")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        if (Easybot.getClient() == null || !Easybot.getClient().isReady()) return;
        if (!joinCooldown.allow(event.getPlayer().getUniqueId().toString(),
                Easybot.instance.getConfig().getInt("sync.join_cooldown_seconds"))) return;
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
        Easybot.EXECUTOR.execute(() -> Easybot.getClient().syncEnterExit(playerInfo, true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(Easybot.instance.getConfig().getBoolean("skip_options.skip_quit")) return;
        if(FakePlayerUtils.isFake(event.getPlayer())) return;
        if (Easybot.getClient() == null || !Easybot.getClient().isReady()) return;
        if (!quitCooldown.allow(event.getPlayer().getUniqueId().toString(),
                Easybot.instance.getConfig().getInt("sync.quit_cooldown_seconds"))) return;
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getPlayer());
        Easybot.EXECUTOR.execute(() -> Easybot.getClient().syncEnterExit(playerInfo, false));
    }
}
