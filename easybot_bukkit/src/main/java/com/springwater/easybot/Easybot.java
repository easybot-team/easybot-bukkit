package com.springwater.easybot;

import com.springwater.easybot.api.CommandApi;
import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.command.EasyBotCommandExecutor;
import com.springwater.easybot.command.SyncCommandExecutor;
import com.springwater.easybot.event.*;
import com.springwater.easybot.hook.HookerManager;
import com.springwater.easybot.papi.EasyBotExpansion;
import com.springwater.easybot.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Objects;

public final class Easybot extends JavaPlugin {
    public static Easybot instance;
    private static HookerManager eventHooks;

    private static CommandApi commandApi;
    private static BridgeClient bridgeClient;
    private static BridgeBehavior bridgeBehavior;
    private static UpdateChecker updateChecker = new UpdateChecker();
    EasyBotExpansion easyBotExpansion;

    public static BridgeClient getClient() {
        return bridgeClient;
    }

    public static CommandApi getCommandApi() {
        return commandApi;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (Objects.equals(getConfig().getString("service.token", ""), "")) {
            Bukkit.getPluginManager().disablePlugin(this);
            getLogger().severe("EasyBot已禁用, 请先在配置文件中设置Token!!!");
            return;
        }

        ClientProfile.setPluginVersion(getDescription().getVersion());
        ClientProfile.setServerDescription(BukkitUtils.tryGetServerDescription());

        instance = this;
        bridgeBehavior = new EasyBotImpl();

        initHooks();

        Objects.requireNonNull(Bukkit.getPluginCommand("easybot")).setExecutor(new EasyBotCommandExecutor());
        Objects.requireNonNull(Bukkit.getPluginCommand("esay")).setExecutor(new SyncCommandExecutor());
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathSyncEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinExitEvents(), this);
        if(BukkitUtils.isPaperMessageEvent()){
            getServer().getPluginManager().registerEvents(new PaperSideMessageSyncEvents(), this);
        }else{
            getServer().getPluginManager().registerEvents(new BukkitSideMessageSyncEvents(), this);
        }

        bridgeClient = new BridgeClient(getConfig().getString("service.url", "ws://127.0.0.1:8080/bridge"), bridgeBehavior);
        bridgeClient.setToken(getConfig().getString("service.token"));
        updateChecker.start();
    }

    public void reload(){
        updateChecker.stop();
        reloadConfig();
        bridgeClient.setToken(getConfig().getString("service.token"));
        bridgeClient.resetUrl(getConfig().getString("service.url", "ws://127.0.0.1:8080/bridge"));
        bridgeClient.stop();
        updateChecker.start();
    }

    private void initHooks() {
        getLogger().info("正在初始化功能");
        getLogger().info("1.命令执行");
        try {
            commandApi = new CommandApi();
            getLogger().info("  ✔ 命令执行");
            ClientProfile.setCommandSupported(true);
        } catch (IllegalAccessException e) {
            getLogger().severe(e.toString());
            getLogger().severe("× 命令执行");
            getLogger().warning("    -> 执行命令 - 命令模式 无法使用!!");
            getLogger().warning("    -> 远程命令 无法使用!!");
            ClientProfile.setCommandSupported(false);
        }

        getLogger().info("2.占位符");
        if (BukkitUtils.placeholderApiInstalled()) {
            if (easyBotExpansion != null) {
                uninstallPlaceholderApi();
            }
            easyBotExpansion = new EasyBotExpansion();
            easyBotExpansion.register();
            getLogger().info("  ✔ 占位符");
            ClientProfile.setPapiSupported(true);
        } else {
            getLogger().warning("× 占位符 (未检测到PlaceholderApi,一些功能无法使用!)");
            getLogger().warning("    -> 执行命令 - Papi模式 无法使用!!");
            ClientProfile.setPapiSupported(false);
        }

    }

    private void uninstallPlaceholderApi() {
        if (easyBotExpansion != null && BukkitUtils.placeholderApiInstalled()) {
            easyBotExpansion.unregister();
            easyBotExpansion = null;
        }
    }

    @Override
    public void onDisable() {
        if (bridgeClient != null) {
            bridgeClient.close();
            bridgeClient = null;
        }
        uninstallPlaceholderApi();
        updateChecker.stop();
    }

    public void runTask(Runnable task) {
        if (BukkitUtils.isFolia()) {
            try {
                Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
                Method getGlobalRegionSchedulerMethod = bukkitClass.getDeclaredMethod("getGlobalRegionScheduler");
                Object regionScheduler = getGlobalRegionSchedulerMethod.invoke(null);

                Method executeMethod = regionScheduler.getClass().getMethod("execute", org.bukkit.plugin.Plugin.class, Runnable.class);
                executeMethod.invoke(regionScheduler, instance, task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(instance, task);
        }
    }
}