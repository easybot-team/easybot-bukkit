package com.springwater.easybot.utils;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatFilterUtils {
    public static void validateConfig(FileConfiguration config) {
        for (String section : Arrays.asList("chat_filter", "chat_filter.game_to_group", "chat_filter.group_to_game", "command", "command.enabled")) {
            if (config.contains(section) && !config.isConfigurationSection(section))
                throw new IllegalArgumentException(section + " 必须是配置节");
        }
        if (config.contains("command.allow_bind") && !config.isBoolean("command.allow_bind"))
            throw new IllegalArgumentException("command.allow_bind 必须是布尔值");
        for (String direction : Arrays.asList("game_to_group", "group_to_game")) {
            String prefix = "chat_filter." + direction + ".";
            if (config.contains(prefix + "max_length") && (!config.isInt(prefix + "max_length") || config.getInt(prefix + "max_length") < 0))
                throw new IllegalArgumentException(prefix + "max_length 必须是非负整数");
            for (String key : Arrays.asList("blocked_keywords", "blocked_player_names", "blocked_player_uuids")) {
                if (!config.contains(prefix + key)) continue;
                List<?> list = config.getList(prefix + key);
                if (list == null) throw new IllegalArgumentException(prefix + key + " 必须是列表");
                if (direction.equals("group_to_game") && !key.equals("blocked_keywords") && !list.isEmpty())
                    throw new IllegalArgumentException("group_to_game 不支持玩家名单，主程序未提供发言人身份");
                for (Object value : list) {
                    if (!(value instanceof String) || ((String) value).trim().isEmpty())
                        throw new IllegalArgumentException(prefix + key + " 不能包含空值或非文本值");
                    if (key.equals("blocked_player_uuids") && !UUID.fromString((String) value).toString().equalsIgnoreCase((String) value))
                        throw new IllegalArgumentException("无效的玩家 UUID: " + value);
                }
            }
        }
        ConfigurationSection enabled = config.getConfigurationSection("command.enabled");
        if (enabled != null) for (String key : enabled.getKeys(false)) {
            if (!Arrays.asList("help", "bind", "confirm", "status", "reload", "config", "esay").contains(key) || !enabled.isBoolean(key))
                throw new IllegalArgumentException("无效的命令开关: " + key);
        }
    }

    public static String outgoingRejection(PlayerInfoWithRaw player, String message) {
        FileConfiguration config = Easybot.instance.getConfig();
        String prefix = "chat_filter.game_to_group.";
        if (player.getUuid() != null && !player.getUuid().isEmpty()) {
            for (String name : config.getStringList(prefix + "blocked_player_names"))
                if (name.equalsIgnoreCase(player.getNameRaw())) return "此玩家已被禁止转发消息";
            for (String uuid : config.getStringList(prefix + "blocked_player_uuids"))
                if (uuid.equalsIgnoreCase(player.getUuid())) return "此玩家已被禁止转发消息";
        }
        return rejection(config, prefix, message);
    }

    public static boolean blocksIncoming(String text, List<Segment> segments) {
        FileConfiguration config = Easybot.instance.getConfig();
        String prefix = "chat_filter.group_to_game.";
        if (rejection(config, prefix, text) != null) return true;
        if (segments == null) return false;
        StringBuilder plain = new StringBuilder();
        for (Segment segment : segments) {
            if (segment != null && segment.getText() != null) plain.append(segment.getText());
        }
        return rejection(config, prefix, plain.toString()) != null;
    }

    private static String rejection(FileConfiguration config, String prefix, String text) {
        if (text == null) return null;
        String plain = text.replaceAll("(?i)§[0-9A-FK-ORX]", "");
        int max = config.getInt(prefix + "max_length", 0);
        if (max > 0 && plain.codePointCount(0, plain.length()) > max) return "消息超过转发长度限制";
        String lower = plain.toLowerCase(Locale.ROOT);
        for (String keyword : config.getStringList(prefix + "blocked_keywords")) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) return "消息包含禁止转发的关键词";
        }
        return null;
    }
}
