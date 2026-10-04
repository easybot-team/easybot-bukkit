package com.springwater.easybot.command;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.utils.ChatFilterUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

final class CooldownConfigCommand {
    static final List<String> KEYS = Arrays.asList("sync.join_cooldown_seconds",
            "sync.quit_cooldown_seconds", "sync.death_cooldown_seconds");

    static synchronized boolean execute(CommandSender sender, String[] args) {
        if (!CommandPermissions.canUse(sender, "config")) {
            sender.sendMessage("§c此命令已禁用或没有权限");
            return true;
        }
        if (args.length == 1) {
            for (String key : KEYS) show(sender, key);
            sender.sendMessage("§7用法: /easybot config <配置项> [秒数]，0 或负数关闭冷却");
            return true;
        }
        if (!KEYS.contains(args[1]) || args.length > 3) {
            sender.sendMessage("§c仅支持三个播报冷却配置项。用法: /easybot config <配置项> [秒数]");
            return true;
        }
        if (args.length == 2) {
            show(sender, args[1]);
            return true;
        }
        final int seconds;
        try {
            seconds = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c秒数必须是 -2147483648 到 2147483647 之间的整数");
            return true;
        }
        Path temporary = null;
        try {
            Path file = new File(Easybot.instance.getDataFolder(), "config.yml").toPath();
            // Load explicitly so malformed configuration is never overwritten with defaults.
            YamlConfiguration saved = new YamlConfiguration();
            saved.load(file.toFile());
            saved.set(args[1], seconds);
            ChatFilterUtils.validateConfig(saved);
            temporary = Files.createTempFile(file.getParent(), "easybot-config-", ".tmp");
            saved.save(temporary.toFile());
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            Easybot.instance.getConfig().set(args[1], seconds);
            sender.sendMessage("§a已保存并生效: " + args[1] + " = " + seconds + " 秒");
        } catch (Exception e) {
            Easybot.instance.getLogger().warning("保存播报冷却配置失败: " + e.getMessage());
            sender.sendMessage("§c保存失败，冷却配置未更改，请检查服务器日志");
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (Exception e) { Easybot.instance.getLogger().warning("清理临时配置失败: " + e.getMessage()); }
            }
        }
        return true;
    }

    private static void show(CommandSender sender, String key) {
        sender.sendMessage("§a" + key + " = " + Easybot.instance.getConfig().getInt(key, 0) + " 秒");
    }
}
