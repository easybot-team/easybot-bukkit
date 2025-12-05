package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.utils.BukkitUtils;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PapiReloadEvents implements Listener {
    @EventHandler
    public void onReload(me.clip.placeholderapi.events.ExpansionsLoadedEvent event){
        if(!PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansions().stream().anyMatch(x -> x == Easybot.instance.easyBotExpansion)){
            Easybot.easyBotExpansion.register();
            Easybot.instance.getLogger().info("§a[OK]§f 已经重新注册EasyBot核心变量");
        }

        if(!PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansions().stream().anyMatch(x -> x == Easybot.instance.offlineStatisticExpansion) && BukkitUtils.isSupportStatistic()){
            Easybot.offlineStatisticExpansion.register();
            Easybot.instance.getLogger().info("§a[OK]§f 已经重新注册离线变量");
        }
    }
}
