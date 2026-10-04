package com.springwater.easybot.command;

import com.springwater.easybot.Easybot;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class CommandPermissions {
    public static boolean canUse(CommandSender sender, String command) {
        FileConfiguration config = Easybot.instance.getConfig();
        if (!config.getBoolean("command.enabled." + command, true)) return false;
        if (!command.equals("esay") && !sender.hasPermission("easybot.command")) return false;
        switch (command) {
            case "bind":
                return config.getBoolean("command.allow_bind", true) && sender.hasPermission("easybot.command.bind");
            case "confirm":
                return canUse(sender, "bind");
            case "status":
                return sender.hasPermission("easybot.command.bind");
            case "reload":
                return sender.hasPermission("easybot.command.reload");
            case "config":
                return sender.hasPermission("easybot.command.config");
            case "esay":
                return sender.hasPermission("easybot.command.esay");
            case "help":
                return true;
            default:
                return false;
        }
    }
}
