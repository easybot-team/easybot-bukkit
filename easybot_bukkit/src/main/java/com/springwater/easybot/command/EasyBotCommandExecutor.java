package com.springwater.easybot.command;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.GetSocialAccountResultPacket;
import com.springwater.easybot.bridge.packet.StartBindResultPacket;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EasyBotCommandExecutor implements TabExecutor {
    private static final String defaultStart =
            "§f[§a!§f] 开始绑定,请在群 §e123456 §f输入 '绑定 #code' 进行绑定!\n" +
                    "§f[§c!§f] 请在§a #time §f前完成验证,到时将自动取消绑定!";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args == null || args.length == 0) {
            if (!sender.hasPermission("easybot.command.bind")) return false;
            sender.sendMessage("§f[§a!§f] §a请输入/easybot bind §f绑定你的账号");
            return true;
        }
        FileConfiguration config = Easybot.instance.getConfig();
        if (args[0].equalsIgnoreCase("bind")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("§f[§a!§f] §aBro,你要给控制台绑定账号是吧? [该命令只能由玩家执行]");
                return true;
            }
            if (!sender.hasPermission("easybot.command.bind")) return false;
            if (!config.getBoolean("command.allow_bind")) return false;

            new Thread(() -> {
                try {
                    GetSocialAccountResultPacket packet = Easybot.getClient().getSocialAccount(sender.getName());
                    if (!Objects.equals(packet.getName(), "")) {
                        sender.sendMessage("§f你的账号已于§a" + packet.getTime() + "§f在§a" + packet.getPlatform() + "§f上被§a" + packet.getName() + "§f(§a" + packet.getUuid() + "§f)绑定。");
                        sender.sendMessage("§c你已经绑定过账号了,请先解绑再绑定。");
                        return;
                    }

                    StartBindResultPacket startBindResultPacket = Easybot.getClient().startBind(sender.getName());
                    String message = config.getString("message.bind_start", defaultStart)
                            .replace("#code", startBindResultPacket.getCode())
                            .replace("#time", startBindResultPacket.getTime())
                            .replace("&", "§");
                    sender.sendMessage(message);
                } catch (Exception ex) {
                    ex.printStackTrace();

                    String message = config.getString("message.bind_fail", "§f[§c!§f] §c绑定失败 #why")
                            .replace("#why", "服务器内部异常")
                            .replace("&", "§");
                    sender.sendMessage(message);
                    if (sender.isOp()) {
                        sender.sendMessage("发生异常,请检查服务器后台:" + ex.getMessage());

                    }
                }
            }).start();
        }else{
            sender.sendMessage("§f[§a!§f] §a请输入/easybot bind §f绑定你的账号");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length != 1) return Arrays.asList();
        return Arrays.asList("bind");
    }
}
