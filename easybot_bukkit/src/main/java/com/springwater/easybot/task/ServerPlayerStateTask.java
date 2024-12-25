package com.springwater.easybot.task;

import com.springwater.easybot.Easybot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ServerPlayerStateTask implements Runnable {
    private static List<Player> getPlayersSafe() {
        try {
            return new ArrayList<>(Bukkit.getOnlinePlayers());
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    public void run() {
        List<Player> players = getPlayersSafe();
        StringBuilder sb = new StringBuilder();
        for (Player player : players) {
            try {
                sb.append(player.getName()).append(",");
            } catch (Exception ignored) {
            }
        }
        System.out.println("\"" + sb + "\"");
        Easybot.getClient().serverState(sb.toString());
    }
}
