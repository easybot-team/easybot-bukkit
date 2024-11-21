package com.springwater.easybot.papi;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.GetBindInfoResultPacket;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EasyBotExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "easybot";
    }

    @Override
    public @NotNull String getAuthor() {
        return Easybot.instance.getDescription().getAuthors().stream().findFirst().orElse("MiuxuE");
    }

    @Override
    public @NotNull String getVersion() {
        return Easybot.instance.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return onRequest(player, params);
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.startsWith("bind_id")) {
            return getBindInfo(getQueryName(player, "bind_id_", params)).getId();
        } else if (params.startsWith("bind_name")) {
            return getBindInfo(getQueryName(player, "bind_name_", params)).getName();
        } else if (params.startsWith("is_bind")) {
            return Objects.equals(getBindInfo(getQueryName(player, "is_bind_", params)).getPlatform(), "") ? "否" : "是";
        } else if (params.startsWith("bind_players")) {
            return getBindInfo(getQueryName(player, "bind_players_", params)).getBindName();
        }
        return super.onRequest(player, params);
    }

    private String getQueryName(OfflinePlayer player, String root, String params) {
        if (player != null) {
            return player.getName();
        }
        if (params.length() > root.length()) {
            return params.substring(root.length());
        }
        return "";
    }

    private GetBindInfoResultPacket getBindInfo(String playerName) {
        try{
            return Easybot.getClient().getBindInfo(playerName);
        }catch (Exception ex){
            ex.printStackTrace();
            GetBindInfoResultPacket packet = new GetBindInfoResultPacket();
            packet.setBindName("");
            packet.setPlatform("");
            packet.setName("");
            packet.setId("");
            return packet;
        }
    }
}