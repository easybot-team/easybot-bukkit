package com.springwater.easybot.event.authme;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.AuthMeUtils;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * 监听 AsyncPlayerConnectionConfigureEvent 事件
 * 在 AuthMe 显示预加入 Dialog 之前，判断玩家是否需要登录
 * <p>
 * 此事件仅在 Paper 1.21.6+ 服务器上可用
 */
public class AuthMePreLoginDialogEvents implements Listener {
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerConfigure(AsyncPlayerConnectionConfigureEvent event) {
        if (!AuthMeUtils.isAuthMeInstalled() || !AuthMeUtils.isDialogSupported()) {
            return;
        }
        String playerName = event.getConnection().getProfile().getName();
        UUID playerId = event.getConnection().getProfile().getId();
        if (playerName == null || playerId == null) {
            return;
        }
        String ip = event.getConnection().getClientAddress().getAddress().getHostAddress();
        boolean isRegistered = AuthMeUtils.isPlayerRegistered(playerName);

        PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
        playerInfo.setIp(ip);
        playerInfo.setName(playerName);
        playerInfo.setUuid(playerId.toString());
        playerInfo.setNameRaw(playerName);

        if (isRegistered) {
            Easybot.EXECUTOR.submit(() -> {
                BridgeClient.getInstance().reportPlayer(playerName, playerId.toString(), ip);
                BridgeClient.getInstance().syncEnterExit(playerInfo, true);
            });
        }
    }
}
