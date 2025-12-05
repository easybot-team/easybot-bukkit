package com.springwater.easybot.command;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SyncCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String message = args != null ? String.join(" ", args) : "";
        if (ClientProfile.getSyncMessageMoney() > 0 && sender instanceof Player) {
            Player player = (Player) sender;
            if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
                throw new IllegalStateException("服务器Vault前置插件可能未配置成功,请联系管理员!");
            }
            Economy economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            EconomyResponse response = economy.withdrawPlayer(player, (double) ClientProfile.getSyncMessageMoney());
            if (!response.transactionSuccess()) {
                player.sendMessage("你没有足够的金钱!");
                return true;
            }
        }

        PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
        playerInfo.setIp("localhost");
        playerInfo.setName("CONSOLE");
        playerInfo.setNameRaw("CONSOLE");
        playerInfo.setUuid("");

        if(sender instanceof Player){
            playerInfo = BridgeUtils.buildPlayerInfoFull((Player)sender);
        }

        PlayerInfoWithRaw finalPlayerInfo = playerInfo;
        new Thread(() -> {
            Easybot.getClient().syncMessage(finalPlayerInfo, message, true);
        }, "EasyBotThread-SyncMessageOnCommand").start();

        sender.sendMessage(Easybot.instance.getConfig().getString("message.sync_success", "§f[§a!§f] §f发送成功!"));
        return true;
    }

    private String getUuid(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId().toString();
        } else {
            return "00000000-0000-0000-0000-000000000000";
        }
    }
}

