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
import com.springwater.easybot.papi.OfflineStatisticExpansion;
import com.springwater.easybot.task.TaskManager;
import com.springwater.easybot.utils.BukkitUtils;
import com.springwater.easybot.utils.ItemsAdderUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.lang.reflect.Method;
import java.util.Objects;

public final class Easybot extends JavaPlugin implements Listener {
    public static Easybot instance;
    private static HookerManager eventHooks;

    private static CommandApi commandApi;
    private static BridgeClient bridgeClient;
    private static BridgeBehavior bridgeBehavior;
    private static UpdateChecker updateChecker = new UpdateChecker();
    private static TaskManager taskManager = new TaskManager();

    public static EasyBotExpansion easyBotExpansion;
    public static OfflineStatisticExpansion offlineStatisticExpansion;

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
        ClientProfile.setDebugMode(getConfig().getBoolean("debug", false));

        instance = this;
        bridgeBehavior = new EasyBotImpl();

        initHooks();

        Objects.requireNonNull(Bukkit.getPluginCommand("easybot")).setExecutor(new EasyBotCommandExecutor());
        Objects.requireNonNull(Bukkit.getPluginCommand("esay")).setExecutor(new SyncCommandExecutor());
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathSyncEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinExitEvents(), this);
        getServer().getPluginManager().registerEvents(this, this);

        onlineModeCheck();
        handleChatsCompatibility();
        handleGeyserCompatibility();
        handleBungeeChatCompatibility();
        handleSkinsRestorerCompatibility();
        handleItemsAdderCompatibility();

        bridgeClient = new BridgeClient(getConfig().getString("service.url", "ws://127.0.0.1:8080/bridge"), bridgeBehavior);
        bridgeClient.setToken(getConfig().getString("service.token"));
        updateChecker.start();
        putTasks();
    }


    private void handleSkinsRestorerCompatibility() {
        if(BukkitUtils.hasSkinsRestorer() && !BukkitUtils.placeholderApiInstalled()){
            getLogger().info("\u001B[31m※ 检测到SkinsRestorer插件,但未检测到PlaceholderApi插件,EasyBot获取SkinsRestorer的皮肤需要依赖PlaceholderApi！\u001B[0m");
        }

        if (BukkitUtils.hasSkinsRestorer() &&   BukkitUtils.placeholderApiInstalled()) {
            getLogger().info("\u001B[32m※ 检测到SkinsRestorer插件,玩家皮肤将通过该插件获取！\u001B[0m");
            ClientProfile.setHasSkinsRestorer(true);
        } else if (BukkitUtils.hasPaperSkinApi()) {
            getLogger().info("\u001B[32m※ 检测到Paper皮肤API,玩家皮肤将通过该接口获取(正版)！\u001B[0m");
            ClientProfile.setHasPaperSkinApi(true);
        } else if (!ClientProfile.isOnlineMode()) {
            getLogger().info("\u001B[31m※ 当前服务器为离线服务器,且你并未安装\u001B[32mSkinsRestorer\u001B[31m插件,这会导致获取玩家皮肤不正确！\u001B[0m");
        }
    }

    private void handleItemsAdderCompatibility() {
        if(ItemsAdderUtils.isItemsAdderInstalled()){
            getLogger().info("\u001B[32m※ 检测到ItemsAdder插件！\u001B[0m");
            ClientProfile.setHasItemsAdder(true);
            Bukkit.getPluginManager().registerEvents(new ItemsAdderEvents(), this);
        }

    }

    private void onlineModeCheck() {
        try {
            ClientProfile.setOnlineMode(Bukkit.getServer().getOnlineMode());
            if (ClientProfile.isOnlineMode()) {
                getLogger().info("\u001B[32m※ 当前服务器已开启正版验证\u001B[0m");
            }
        } catch (Exception ignored) {
        }
    }

    private void handleBungeeChatCompatibility() {
        ClientProfile.setHasBungeeChatApi(BukkitUtils.hasBungeeChatApi());
        if (ClientProfile.isHasBungeeChatApi()) {
            getLogger().info("\u001B[32m※ 您的服务器支持消息同步高级API\u001B[0m");
        } else {
            getLogger().info("\u001B[32m※ 您的服务端不支持消息同步高级API! 消息同步将以旧版本格式展示!\u001B[0m");
        }
    }

    private void handleGeyserCompatibility() {
        ClientProfile.setHasGeyser(BukkitUtils.hasGeyserMc());
        ClientProfile.setHasFloodgate(BukkitUtils.hasFloodgate());
        if (ClientProfile.isHasGeyser()) {
            getLogger().info("\u001B[32m※ 检测到GeyserMC插件\u001B[0m");
        }
        if (ClientProfile.isHasFloodgate()) {
            getLogger().info("\u001B[32m※ 检测到Floodgate插件\u001B[0m");

            String userNamePrefix = FloodgateApi.getInstance().getPlayerPrefix();
            if (userNamePrefix != null) {
                getLogger().info("\u001B[32m - 基岩版用户前缀: " + userNamePrefix + "\u001B[0m");
                if (getConfig().getBoolean("geyser.ignore_prefix")) {
                    getLogger().info("\u001B[32m - 注意: EasyBot会在处理数据时忽略玩家前缀: " + userNamePrefix + "MiuxuE" + " -> " + "MiuxuE" + "\u001B[0m");
                } else {
                    getLogger().info("\u001B[32m - 您可以设置为忽略前缀, 忽略前缀后玩家将不再有前缀,例:" + userNamePrefix + "MiuxuE" + " -> " + userNamePrefix + "MiuxuE" + "\u001B[0m");
                    getLogger().info("\u001B[32m - 请参考配置文件: geyser.ignore_prefix\u001B[0m");
                }
            }
        }
    }

    private void handleChatsCompatibility() {
        if (BukkitUtils.hasPlayerChatPlugin()) {
            getLogger().info("\u001B[32m※ 检测到PlayerChat插件, 正在检查可用性...\u001B[0m");

            if (BukkitUtils.canUsePlayerChatEvent()) {
                getLogger().info("\u001B[32m - 您的PlayerChat可以兼容!\u001B[0m");
                getServer().getPluginManager().registerEvents(new PlayerChatMessageSyncEvents(), this);
                return;
            }

            getLogger().warning("\u001B[31m - 检测到PlayerChat插件, 但是无法兼容, 请升级PlayerChat到\u001B[32mv1.1.7\u001B[31m以上版本!!!!\u001B[0m");
            getLogger().warning("\u001B[31m - 正在使用原版消息事件, 消息同步可能无法使用, 请升级PlayerChat到\u001B[32mv1.1.7\u001B[31m以上版本!!!!\u001B[0m");
            registerDefaultMessageSyncEvents();
        } else if (BukkitUtils.hasRedisChatPlugin()) {
            getLogger().info("\u001B[32m※ 检测到RedisChat插件, 您的消息事件将对接到AsyncRedisChatMessageEvent\u001B[0m");
            getServer().getPluginManager().registerEvents(new RedisChatMessageSyncEvents(), this);
        } else {
            registerDefaultMessageSyncEvents();
        }
    }


    private void registerDefaultMessageSyncEvents() {
        if (BukkitUtils.isPaperMessageEvent()) {
            getServer().getPluginManager().registerEvents(new PaperSideMessageSyncEvents(), this);
        } else {
            getServer().getPluginManager().registerEvents(new BukkitSideMessageSyncEvents(), this);
        }
    }

    public void reload() {
        updateChecker.stop();
        reloadConfig();
        ClientProfile.setPluginVersion(getDescription().getVersion());
        ClientProfile.setServerDescription(BukkitUtils.tryGetServerDescription());
        ClientProfile.setDebugMode(getConfig().getBoolean("debug", false));
        restartNativeRcon();
        bridgeClient.setToken(getConfig().getString("service.token"));
        bridgeClient.resetUrl(getConfig().getString("service.url", "ws://127.0.0.1:8080/bridge"));
        bridgeClient.stop();
        updateChecker.start();
        putTasks();
    }

    private void putTasks() {
        taskManager.clearAllTasks();
        //taskManager.addTask(Tasks.TASK_SERVER_PLAYER_STATE, 60, new ServerPlayerStateTask());
    }

    private void initHooks() {
        getLogger().info("正在初始化功能");
        getLogger().info("\u001B[32m[>]\u001B[0m 命令执行");
        try {
            commandApi = new CommandApi();
            getLogger().info("  \u001B[32m[OK]\u001B[0m 命令执行");
            ClientProfile.setCommandSupported(true);
        } catch (IllegalAccessException e) {
            getLogger().severe(e.toString());
            getLogger().severe("× 命令执行");
            getLogger().warning("    -> 执行命令 - 命令模式 无法使用!!");
            getLogger().warning("    -> 远程命令 无法使用!!");
            ClientProfile.setCommandSupported(false);
        }

        getLogger().info("\u001B[32m[>]\u001B[0m 占位符");
        if (BukkitUtils.placeholderApiInstalled()) {
            if (easyBotExpansion != null) {
                uninstallPlaceholderApi();
            }
            easyBotExpansion = new EasyBotExpansion();
            easyBotExpansion.register();

            if (BukkitUtils.isSupportStatistic()) {
                offlineStatisticExpansion = new OfflineStatisticExpansion();
                offlineStatisticExpansion.register();
            } else {
                getLogger().warning("× 离线变量 (ez-statistic只支持1.15+的服务器!)");
            }

            getServer().getPluginManager().registerEvents(new PapiReloadEvents(), this);

            getLogger().info("  \u001B[32m[OK]\u001B[0m 占位符");
            ClientProfile.setPapiSupported(true);
        } else {
            getLogger().warning("× 占位符 (未检测到PlaceholderApi,一些功能无法使用!)");
            getLogger().warning("    -> 执行命令 - Papi模式 无法使用!!");
            ClientProfile.setPapiSupported(false);
        }

        if (BukkitUtils.placeholderApiInstalled() && offlineStatisticExpansion != null) {
            getLogger().info("\u001B[32m※ 已注册离线变量,专用文档: \u001B[33mhttps://docs.hualib.com/offline-papi.html\u001B[0m");
        }

    }

    private void uninstallPlaceholderApi() {
        try {
            if (BukkitUtils.placeholderApiInstalled()) {
                if (easyBotExpansion != null) {
                    easyBotExpansion.unregister();
                    easyBotExpansion = null;
                }

                if (offlineStatisticExpansion != null) {
                    offlineStatisticExpansion.unregister();
                    offlineStatisticExpansion = null;
                }
            }
        } catch (Exception e) {
            // ignore
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
        taskManager.clearAllTasks();
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

    @EventHandler
    public void onServerStarted(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.STARTUP) {
            boolean useNativeRcon = getConfig().getBoolean("adapter.native_rcon.use_native_rcon", false);
            if (!useNativeRcon) return;
            Thread rconThread = new Thread(() -> {
                try {
                    getLogger().info("10s后启动原生RCON,请耐心等待!");
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
                restartNativeRcon();
            }, "EasyBot-Rcon-Thread");
            rconThread.start();
        }
    }

    private void restartNativeRcon() {
        try {
            boolean useNativeRcon = getConfig().getBoolean("adapter.native_rcon.use_native_rcon", false);
            if (useNativeRcon) {
                commandApi.closeNativeRcon();
                commandApi = new CommandApi();
                commandApi.startNativeRcon();
            }
        } catch (Exception e) {
            getLogger().severe(e + "");
            getLogger().warning("原生RCON启动失败,您无法通过命令Api执行命令!");
            ClientProfile.setCommandSupported(false);
        }
    }
}