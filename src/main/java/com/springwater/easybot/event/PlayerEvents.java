package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerLoginResultPacket;
import com.springwater.easybot.utils.CompatUtils;
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
            if (name == null) name = event.getName();
            Easybot.getClient().reportPlayer(name, GeyserUtils.getUuid(event.getUniqueId()).toString(), ip);
            PlayerLoginResultPacket result = Easybot.getClient().login(
                    name,
                    GeyserUtils.getUuid(event.getUniqueId()).toString()
            );
            if (result.getKick()) {
                disallow(event, result.getKickMessage());
            }
        } catch (Exception ex) {
            Easybot.instance.getLogger().severe("处理玩家登录事件遇到异常! " + ex);
            if (!Easybot.instance.getConfig().getBoolean("service.ignore_error")) {
                disallow(event, "§c服务器内部异常,请稍后重试!");
            }
        }
    }

    /**
     * 拒绝玩家登录。
     * <p>
     * Paper 26.x 推荐使用 {@code disallow(Result, Component)}，
     * 而 {@code setKickMessage(String)} 已逐步弃用，这里优先新接口、旧服务端自动回退。
     */
    private void disallow(AsyncPlayerPreLoginEvent event, String message) {
        if (CompatUtils.disallowPreLogin(event, message)) return;
        event.setKickMessage(message);
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    }
}
