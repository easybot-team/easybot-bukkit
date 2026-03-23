package com.springwater.easybot.utils;

import lombok.Getter;
import org.bukkit.entity.Player;

public class AuthMeUtils {
    @Getter
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

    public static Boolean isPlayerAuthenticated(Player player) {
        if (!isAuthMeInstalled) return true;
        return fr.xephi.authme.api.v3.AuthMeApi.getInstance().isAuthenticated(player);
    }
}
