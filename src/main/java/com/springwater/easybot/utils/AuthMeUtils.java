package com.springwater.easybot.utils;

import com.springwater.easybot.Easybot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class AuthMeUtils {
    private static boolean isAuthMeInstalled = false;

    public static boolean init() {
        try {
            Class.forName("fr.xephi.authme.AuthMe");
            isAuthMeInstalled = true;
        } catch (ClassNotFoundException e) {
            isAuthMeInstalled = false;
        }
        return isAuthMeInstalled;
    }

    public Boolean isPlayerAuthenticated(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Boolean> inner = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(Easybot.instance, () -> {
                try {
                    Player player = Bukkit.getPlayer(playerName);
                    boolean result = player != null &&
                            fr.xephi.authme.api.v3.AuthMeApi.getInstance().isAuthenticated(player);
                    inner.complete(result);
                } catch (Exception e) {
                    inner.completeExceptionally(e);
                }
            });
            return inner;
        }).thenCompose(f -> f).join();
    }

}
