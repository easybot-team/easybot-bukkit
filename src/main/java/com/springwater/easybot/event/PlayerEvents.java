package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerLoginResultPacket;
import com.springwater.easybot.utils.FakePlayerUtils;
import com.springwater.easybot.utils.GeyserUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerEvents implements Listener {
    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        try {
            if(FakePlayerUtils.isFake(event.getName())) return;
            String ip = event.getAddress().getHostAddress();
            String name = GeyserUtils.getName(event.getUniqueId());
            if (name == null) name = event.getPlayerProfile().getName();
            Easybot.getClient().reportPlayer(name, GeyserUtils.getUuid(event.getUniqueId()).toString(), ip);
            PlayerLoginResultPacket result = Easybot.getClient().login(
                    name,
                    GeyserUtils.getUuid(event.getUniqueId()).toString()
            );
            if (result.getKick()) {
                event.setKickMessage(result.getKickMessage());
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            }
        } catch (Exception ex) {
            Easybot.instance.getLogger().severe("处理玩家登录事件遇到异常! " + ex);
            if (!Easybot.instance.getConfig().getBoolean("service.ignore_error")) {
                event.setKickMessage("§c服务器内部异常,请稍后重试!");
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            }
        }
    }
}
