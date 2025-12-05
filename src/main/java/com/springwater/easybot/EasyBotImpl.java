package com.springwater.easybot;

import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.message.*;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.springwater.easybot.utils.*;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        info.setHasGeyser(ClientProfile.isHasGeyser());
        info.setOnlineMode(ClientProfile.isOnlineMode());
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

    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        try {
            ComponentBuilder builder = new ComponentBuilder("");

            Queue<Segment> queue = new LinkedList<>(segments);
            StringBuilder currentText = new StringBuilder();  // 使用StringBuilder合并文本
            List<Segment> segmentsToAdd = new ArrayList<>();

            while (!queue.isEmpty()) {
                Segment segment = queue.poll();
                if (segment instanceof TextSegment) {
                    currentText.append(segment.getText());  // 直接用StringBuilder追加文本
                } else {
                    if (currentText.length() > 0) {  // 如果有文本累积，则添加合并的TextSegment
                        TextSegment combinedTextSegment = new TextSegment();
                        combinedTextSegment.setText(currentText.toString());
                        segmentsToAdd.add(combinedTextSegment);
                        currentText.setLength(0);  // 重置StringBuilder
                    }
                    segmentsToAdd.add(segment);  // 直接添加非TextSegment的部分
                }
            }

            if (currentText.length() > 0) {
                TextSegment combinedTextSegment = new TextSegment();
                combinedTextSegment.setText(currentText.toString());
                segmentsToAdd.add(combinedTextSegment);
            }

            for (Segment segment : segmentsToAdd) {
                builder.append(toComponent(segment));
            }


            List<String> atPlayerNames = segments.stream()
                    .filter(x -> x instanceof AtSegment)
                    .flatMap(seg -> Arrays.stream(((AtSegment) seg).getAtPlayerNames()))
                    .collect(Collectors.toList());

            Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(p -> {
                // 判断玩家名字是否在atPlayerNames中,忽略大小写
                boolean hasAt = atPlayerNames.stream().anyMatch(x -> x.equalsIgnoreCase(GeyserUtils.getName(p)));
                if (!hasAt && Easybot.instance.getConfig().getBoolean("event.on_at.find", true)) {
                    hasAt = text.contains(GeyserUtils.getName(p));
                }

                if (hasAt && Easybot.instance.getConfig().getBoolean("event.on_at.enable", true)) {
                    AtEventUtils.at(p.getPlayer());
                }

                try {
                    try {
                        p.spigot().sendMessage(builder.create());
                    } catch (Exception ignored) {
                        p.sendMessage(builder.create());
                    }
                } catch (Exception ex) {
                    logger.warning(ex.getMessage());
                    logger.warning("将群内消息转换为Minecraft格式消息时遇到错误,将向玩家发送原始信息!");
                    Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(text)));
                }
            }));
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            logger.warning("将群内消息转换为Minecraft格式消息时遇到错误,将向玩家发送原始信息!");
            Easybot.instance.runTask(() -> Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(text)));
        }
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        return Bukkit.getOnlinePlayers().stream()
                .map(x -> {
                    PlayerInfo info = new PlayerInfo();
                    info.setPlayerName(GeyserUtils.getName(x));
                    info.setPlayerUuid(GeyserUtils.getUuid(x).toString());
                    info.setIp(BridgeUtils.getPlayerIp(x));
                    info.setBedrock(GeyserUtils.isBedrock(x));
                    info.setSkinUrl(SkinUtils.getSkin(x));
                    return info;
                })
                .collect(Collectors.toList());
    }


    private BaseComponent toComponent(Segment segment) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(segment.getText()));
        if (segment instanceof AtSegment) {
            component.setColor(ChatColor.GOLD);
            String[] atPlayerNames = ((AtSegment) segment).getAtPlayerNames();
            if (!Objects.equals(((AtSegment) segment).getAtUserId(), "0")) {
                component.setHoverEvent(
                        new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("")
                                        .append("@")
                                        .append(((AtSegment) segment).getAtUserName())
                                        .append(" (")
                                        .append(((AtSegment) segment).getAtUserId())
                                        .append(")")
                                        .append(
                                                atPlayerNames.length > 1 ?
                                                        new TextComponent(
                                                                "\n该玩家绑定了" + atPlayerNames.length + "个账号\n" + String.join(",", atPlayerNames)
                                                        )
                                                        : new TextComponent("")
                                        )
                                        .create()
                        )
                );
            }
        } else if (segment instanceof ImageSegment) {
            component.setColor(ChatColor.GREEN);
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("§7§n点击预览 ")
                            .append(new TextComponent("§7§n" + ((ImageSegment) segment).getUrl()))
                            .create()
            ));
            component.setClickEvent(new ClickEvent(
                    ClickEvent.Action.OPEN_URL,
                    ((ImageSegment) segment).getUrl()
            ));
        } else if (segment instanceof FileSegment) {
            component.setColor(ChatColor.GOLD);
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("§7§n点击下载 ")
                            .append(new TextComponent("§7§n" + ((FileSegment) segment).getFileUrl()))
                            .create()
            ));
            component.setClickEvent(new ClickEvent(
                    ClickEvent.Action.OPEN_URL,
                    ((FileSegment) segment).getFileUrl()
            ));

        } else if(segment instanceof FaceSegment) {
            component.setColor(ChatColor.GREEN);
            if (ClientProfile.isHasItemsAdder() && ClientProfile.isHasQFaces()){
                String qface = ItemsAdderUtils.getFace(Integer.parseInt(((FaceSegment) segment).getId()));
                if (qface != null){
                    component = new TextComponent(qface);
                    component.setColor(ChatColor.WHITE);
                }
            }
        }
        else
        {
            component.setColor(ChatColor.WHITE);
        }
        return component;
    }
}
