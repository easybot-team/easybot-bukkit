package com.springwater.easybot.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.provider.LibreLoginProvider;

public class LibreLoginUtils {
    private static @Nullable LibreLoginPlugin<Player, World> api = null;

    public static boolean isLibreLoginInstalled() {
        return Bukkit.getPluginManager().isPluginEnabled("LibreLogin");
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    public static boolean isAuthenticated(Player player) {
        if (!isLibreLoginInstalled()) {
            return true;
        }
        if (api == null) {
            api = ((LibreLoginProvider<Player, World>) Bukkit.getPluginManager().getPlugin("LibreLogin")).getLibreLogin();
        }
        return api.getAuthorizationProvider().isAuthorized(player);
    }
}
