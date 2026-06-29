package com.springwater.easybot.utils;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.api.v3.AuthMeApi;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class AuthMeUtils {
    @Getter
    private static boolean isAuthMeInstalled = false;

    @Getter
    private static boolean isDialogSupported = false;

    @Getter
    private static boolean isPreJoinDialogEnabled = false;

    private static AuthMe authMePlugin;
    private static AuthMeApi authMeApi;
    private static Object preJoinDialogService;
    private static Method approvePreJoinForceLoginMethod;

    public static boolean init() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AuthMe");
        if (!(plugin instanceof AuthMe)) {
            isAuthMeInstalled = false;
            return false;
        }

        authMePlugin = (AuthMe) plugin;
        authMeApi = AuthMeApi.getInstance();
        isAuthMeInstalled = true;

        try {
            initDialogSupport();
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EasyBot] 初始化 AuthMe Dialog 支持失败: " + e.getMessage());
            e.printStackTrace();
            isDialogSupported = false;
        }

        return true;
    }

    private static void initDialogSupport() throws Exception {
        // 获取 AuthMe 版本
        String version = authMePlugin.getDescription().getVersion();
        int majorVersion = parseMajorVersion(version);
        if (majorVersion < 6) {
            isDialogSupported = false;
            Bukkit.getLogger().info("[EasyBot] AuthMe 版本 " + version + " 不支持 Dialog（需要 6.0.0+）");
            return;
        }

        // 检查 Dialog API 类是否存在
        try {
            Class.forName("io.papermc.paper.dialog.Dialog");
            isDialogSupported = true;
        } catch (ClassNotFoundException e) {
            // 不是 Paper/Folia 服务器，检查 Spigot Dialog API
            try {
                Class.forName("net.md_5.bungee.api.dialog.MultiActionDialog");
                isDialogSupported = true;
            } catch (ClassNotFoundException e2) {
                isDialogSupported = false;
                Bukkit.getLogger().info("[EasyBot] 服务器不支持 Dialog API");
                return;
            }
        }

        // 通过反射获取 injector（因为 injector 是私有字段）
        Field injectorField = AuthMe.class.getDeclaredField("injector");
        injectorField.setAccessible(true);
        Object injector = injectorField.get(authMePlugin);

        if (injector == null) {
            isDialogSupported = false;
            Bukkit.getLogger().warning("[EasyBot] AuthMe injector 为 null，可能插件未完全初始化");
            return;
        }

        // 获取 getSingleton 方法
        Class<?> injectorClass = injector.getClass();
        Method getSingletonMethod = injectorClass.getMethod("getSingleton", Class.class);

        // 获取 PreJoinDialogService
        Class<?> preJoinDialogServiceClass = Class.forName("fr.xephi.authme.service.PreJoinDialogService");
        preJoinDialogService = getSingletonMethod.invoke(injector, preJoinDialogServiceClass);

        // 获取 approvePreJoinForceLogin 方法
        approvePreJoinForceLoginMethod = preJoinDialogServiceClass.getMethod(
            "approvePreJoinForceLogin", String.class);

        // 检查 PreJoinDialog 配置
        checkPreJoinDialogConfig(injector, getSingletonMethod);

        Bukkit.getLogger().info("[EasyBot] AuthMe Dialog 支持已初始化，PreJoinDialog: " + isPreJoinDialogEnabled);
    }

    private static void checkPreJoinDialogConfig(Object injector, Method getSingletonMethod) throws Exception {
        // 获取 CommonService
        Class<?> commonServiceClass = Class.forName("fr.xephi.authme.service.CommonService");
        Object commonService = getSingletonMethod.invoke(injector, commonServiceClass);

        // 获取 getProperty 方法
        Class<?> propertyClass = Class.forName("ch.jalu.configme.properties.Property");
        Method getPropertyMethod = commonServiceClass.getMethod("getProperty", propertyClass);

        // 获取 RegistrationSettings.USE_PREJOIN_DIALOG_UI
        Class<?> registrationSettingsClass = Class.forName("fr.xephi.authme.settings.properties.RegistrationSettings");
        Field preJoinDialogField = registrationSettingsClass.getField("USE_PREJOIN_DIALOG_UI");
        Object preJoinDialogProperty = preJoinDialogField.get(null);

        isPreJoinDialogEnabled = (boolean) getPropertyMethod.invoke(commonService, preJoinDialogProperty);
    }

    private static int parseMajorVersion(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts.length > 0) {
                return Integer.parseInt(parts[0]);
            }
        } catch (NumberFormatException e) {
            // 忽略
        }
        return 0;
    }

    /**
     * 检查玩家是否已登录
     */
    public static boolean isPlayerAuthenticated(Player player) {
        if (!isAuthMeInstalled || authMeApi == null) return true;
        return authMeApi.isAuthenticated(player);
    }

    /**
     * 检查玩家是否已注册
     */
    public static boolean isPlayerRegistered(String playerName) {
        if (!isAuthMeInstalled || authMeApi == null) return false;
        return authMeApi.isRegistered(playerName);
    }

    /**
     * 检查玩家是否正在等待预加入 Dialog 登录
     */
    public static boolean isPlayerInPreJoinDialog(String playerName) {
        if (!isDialogSupported || preJoinDialogService == null) {
            return false;
        }
        try {
            Field pendingPreJoinByNameField = preJoinDialogService.getClass()
                .getDeclaredField("pendingPreJoinByName");
            pendingPreJoinByNameField.setAccessible(true);

            @SuppressWarnings("unchecked")
            ConcurrentMap<String, UUID> pendingPreJoinByName =
                (ConcurrentMap<String, UUID>) pendingPreJoinByNameField.get(preJoinDialogService);

            String normalizedName = playerName.toLowerCase(Locale.ROOT);
            return pendingPreJoinByName.containsKey(normalizedName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 跳过玩家的预加入 Dialog 登录
     */
    public static boolean skipPreJoinLogin(String playerName) {
        if (!isDialogSupported || preJoinDialogService == null || approvePreJoinForceLoginMethod == null) {
            return false;
        }
        try {
            String normalizedName = playerName.toLowerCase(Locale.ROOT);
            return (boolean) approvePreJoinForceLoginMethod.invoke(preJoinDialogService, normalizedName);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EasyBot] 跳过预加入登录失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 强制登录玩家（需要玩家已在线）
     */
    public static boolean forceLoginPlayer(Player player) {
        if (!isAuthMeInstalled || authMeApi == null) return false;
        try {
            authMeApi.forceLogin(player);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EasyBot] 强制登录失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 综合方法：跳过玩家登录
     */
    public static boolean skipPlayerLogin(Player player, String playerName) {
        if (!isAuthMeInstalled) return false;

        // 先尝试跳过预加入 Dialog
        if (isDialogSupported && isPreJoinDialogEnabled) {
            if (skipPreJoinLogin(playerName)) {
                return true;
            }
        }

        // 如果玩家在线，尝试强制登录
        if (player != null && player.isOnline()) {
            return forceLoginPlayer(player);
        }

        return false;
    }

    /**
     * 检查服务器是否支持 AsyncPlayerConnectionConfigureEvent
     */
    public static boolean isAsyncPlayerConnectionConfigureEventSupported() {
        try {
            Class.forName("io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取 AuthMe Dialog 支持状态的描述
     */
    public static String getDialogSupportStatus() {
        if (!isAuthMeInstalled) {
            return "AuthMe 未安装";
        }

        boolean hasConfigureEvent = isAsyncPlayerConnectionConfigureEventSupported();

        if (!isDialogSupported) {
            if (!hasConfigureEvent) {
                return "Dialog 不支持：服务器不支持 AsyncPlayerConnectionConfigureEvent（需要 Paper 1.21.6+）";
            }
            return "Dialog 不支持：AuthMe 版本低于 6.0.0";
        }

        if (!hasConfigureEvent) {
            return "Dialog 已支持但服务器不支持 PreJoin（需要 Paper 1.21.6+）";
        }

        if (!isPreJoinDialogEnabled) {
            return "Dialog 已支持但 PreJoinDialog 未启用";
        }
        return "PreJoinDialog 已启用";
    }
}
