package com.springwater.easybot;

import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.model.ServerInfo;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class EasyBotImpl implements BridgeBehavior {
    private final Logger logger = Logger.getLogger("EasyBotImpl");

    @Override
    public String runCommand(String playerName, String command, boolean enablePapi) {
        if (!ClientProfile.isCommandSupported()) {
            logger.warning("无法执行命令: 此服务端不支持执行命令,请检查EasyBot之前输出的日志以找到原因!");
            return "无法执行命令: 此服务端不支持执行命令,请检查服务端EasyBot插件之前输出的日志以找到原因!";
        }

        if (!ClientProfile.isPapiSupported() && enablePapi) {
            logger.warning("无法执行EasyBot主程序传来的命令,服务器未安装PlaceholderApi!");
            return "无法执行命令: 服务器未安装PlaceholderApi!";
        }

        if (enablePapi) {
            OfflinePlayer player = null;
            if (playerName != null && !playerName.equals("")) {
                player = Bukkit.getOfflinePlayer(playerName);
            }
            command = PlaceholderAPI.setPlaceholders(player, command);
        }
        return Easybot.getCommandApi().runCommand(command);
    }

    @Override
    public String papiQuery(String playerName, String query) {
        OfflinePlayer player = null;
        if (playerName != null && !playerName.equals("")) {
            player = Bukkit.getOfflinePlayer(playerName);
        }
        return PlaceholderAPI.setPlaceholders(player, query);
    }

    @Override
    public ServerInfo getInfo() {
        ServerInfo info = new ServerInfo();
        info.setServerName(Bukkit.getName());
        info.setPluginVersion(Easybot.instance.getDescription().getVersion());
        info.setServerVersion(Bukkit.getBukkitVersion());
        info.setCommandSupported(ClientProfile.isCommandSupported());
        info.setPapiSupported(ClientProfile.isPapiSupported());
        return info;
    }

    @Override
    public void SyncToChat(String message) {
        logger.info(message);
        Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(message)));
    }

    @Override
    public void BindSuccessBroadcast(String playerName, String accountId, String accountName) {
        Easybot.instance.runTask(() -> {
            Player onlinePlayer = Bukkit.getPlayer(playerName);
            if (onlinePlayer != null) {
                String message = Easybot.instance.getConfig().getString("message.bind_success", "§f[§a!§f] 绑定§f §a#account §f(§a#name§f) 成功!")
                        .replace("&", "§")
                        .replace("#account", accountId)
                        .replace("#name", accountName);
                onlinePlayer.sendMessage(message);
            }

            if (Easybot.instance.getConfig().getBoolean("event.enable_success_event", false)) {
                if (!ClientProfile.isCommandSupported()) {
                    logger.warning("无法在玩家绑定成功后执行命令: 此服务端不支持执行命令,请检查EasyBot之前输出的日志以找到原因!");
                } else {
                    List<String> commands = Easybot.instance.getConfig().getStringList("event.bind_success");
                    for (String command : commands) {
                        command = command
                                .replace("&", "§")
                                .replace("$player", playerName)
                                .replace("$account", accountId)
                                .replace("$name", accountName);
                        Easybot.getCommandApi().runCommandAsConsole(command);
                    }
                }
            }
        });
    }

    @Override
    public void KickPlayer(String player, String kickMessage) {
        Easybot.instance.runTask(() -> {
            Player kickPlayer = Bukkit.getPlayer(player);
            if (kickPlayer != null) {
                kickPlayer.kickPlayer(kickMessage);
            }
        });
    }
}
