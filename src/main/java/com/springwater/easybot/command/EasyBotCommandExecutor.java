package com.springwater.easybot.command;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.TimeoutException;

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
        if(args[0].equalsIgnoreCase("reload")){
            if(!sender.isOp()){
                sender.sendMessage("§f[§a!§f] §c你没有权限执行此命令");
                return true;
            }
            Easybot.instance.reload();
            sender.sendMessage("§f[§a!§f] §a配置文件已重载");
            return true;
        }
        if (args[0].equalsIgnoreCase("bind") && args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("§f[§a!§f] §a该命令只能由玩家执行");
                return true;
            }
            if (!sender.hasPermission("easybot.command.bind")) return false;
            if (!config.getBoolean("command.allow_bind")) return false;

            new Thread(() -> {
                try {
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
        } else if (args[0].equalsIgnoreCase("bind")) {
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
                        try {
                            QueryBindStatusResultPacket statusPacket = Easybot.getClient().queryBindStatus(sender.getName());
                            Easybot.instance.runTask(() -> {
                                sender.sendMessage("§f[§a!§f] §f你已绑定以下社交平台：");
                                if (statusPacket.isBound() && statusPacket.getSocialAccounts() != null) {
                                    for (BindStatusAccount account : statusPacket.getSocialAccounts()) {
                                        sender.sendMessage("§f  §c" + account.getPlatform() + " §f- §c" + account.getName() + " §7(" + account.getUuid() + ")");
                                    }
                                }
                                sender.sendMessage("§f[§a!§f] §f验证码只能用于绑定§c新平台§f，无法重复绑定已有平台");

                                TextComponent confirmBtn = new TextComponent("§a[点我确认]");
                                confirmBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/easybot bind confirm"));
                                confirmBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder("§7点击后将继续生成绑定验证码").create()));
                                ((org.bukkit.entity.Player) sender).spigot().sendMessage(confirmBtn);
                            });
                        } catch (Exception e) {
                            Easybot.instance.runTask(() -> sender.sendMessage("§f[§c!§f] §c查询绑定状态失败，请稍后再试"));
                        }
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
        } else if (args[0].equalsIgnoreCase("confirm")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("§f[§a!§f] §a该命令只能由玩家执行");
                return true;
            }
            if (!sender.hasPermission("easybot.command.bind")) return false;
            if (args.length < 2) {
                sender.sendMessage("§f[§c!§f] §c用法: /easybot confirm <code>");
                return true;
            }
            String code = args[1];
            String playerName = sender.getName();
            new Thread(() -> confirmBind(sender, playerName, code)).start();
        } else if (args[0].equalsIgnoreCase("status")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("§f[§a!§f] §a该命令只能由玩家执行");
                return true;
            }
            if (!sender.hasPermission("easybot.command.bind")) return false;
            String playerName = sender.getName();
            new Thread(() -> queryBindStatus(sender, playerName)).start();
        } else {
            sender.sendMessage("§f[§a!§f] §a请输入/easybot bind §f绑定你的账号");
        }
        return true;
    }

    /**
     * 确认跨平台绑定
     * 异步调用 BridgeClient.confirmBind，完成后切回主线程向玩家展示结果
     *
     * @param sender     命令发送者（玩家）
     * @param playerName 玩家名称
     * @param code       绑定确认码
     */
    private void confirmBind(CommandSender sender, String playerName, String code) {
        try {
            ConfirmBindResultPacket packet = Easybot.getClient().confirmBind(playerName, code);
            Easybot.instance.runTask(() -> {
                sender.sendMessage(packet.getMessage());
                if (packet.isSuccess() && packet.getBoundPlatforms() != null && !packet.getBoundPlatforms().isEmpty()) {
                    sender.sendMessage("§f已绑定平台: §a" + packet.getBoundPlatforms());
                }
            });
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof TimeoutException) {
                Easybot.instance.runTask(() -> sender.sendMessage("§f[§c!§f] §c操作超时，请稍后再试"));
            } else {
                ex.printStackTrace();
                Easybot.instance.runTask(() -> sender.sendMessage("§f[§c!§f] §c操作失败，请稍后再试"));
            }
        }
    }

    /**
     * 查询玩家绑定状态
     * 异步调用 BridgeClient.queryBindStatus，完成后切回主线程向玩家展示绑定的社交平台列表
     *
     * @param sender     命令发送者（玩家）
     * @param playerName 玩家名称
     */
    private void queryBindStatus(CommandSender sender, String playerName) {
        try {
            QueryBindStatusResultPacket packet = Easybot.getClient().queryBindStatus(playerName);
            Easybot.instance.runTask(() -> {
                if (!packet.isBound()) {
                    sender.sendMessage("§f[§a!§f] §f你尚未绑定任何社交平台");
                    return;
                }
                List<BindStatusAccount> accounts = packet.getSocialAccounts();
                if (accounts == null || accounts.isEmpty()) {
                    sender.sendMessage("§f[§a!§f] §f你尚未绑定任何社交平台");
                    return;
                }
                sender.sendMessage("§f[§a!§f] §f已绑定的社交平台:");
                for (BindStatusAccount account : accounts) {
                    sender.sendMessage("§f  §a" + account.getPlatform() + " §f- §e" + account.getName() + " §7(" + account.getUuid() + ")");
                }
            });
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof TimeoutException) {
                Easybot.instance.runTask(() -> sender.sendMessage("§f[§c!§f] §c操作超时，请稍后再试"));
            } else {
                ex.printStackTrace();
                Easybot.instance.runTask(() -> sender.sendMessage("§f[§c!§f] §c操作失败，请稍后再试"));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            return commandSender.isOp() ? Arrays.asList("bind", "confirm", "status", "reload") : Arrays.asList("bind", "confirm", "status");
        }
        if (strings.length == 2 && strings[0].equalsIgnoreCase("bind")) {
            return Collections.singletonList("confirm");
        }
        return Collections.emptyList();
    }
}
