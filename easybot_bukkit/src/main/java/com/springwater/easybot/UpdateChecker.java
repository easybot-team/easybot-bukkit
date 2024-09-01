package com.springwater.easybot;

import com.springwater.easybot.bridge.packet.GetNewVersionResultPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateChecker {

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private boolean firstRun = true;

    public void start() {
        enabled.set(true);
        startUpdateChecking();
    }

    public void stop() {
        enabled.set(false);
        scheduler.shutdownNow();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private void startUpdateChecking() {
        scheduler.scheduleAtFixedRate(() -> {
            if (enabled.get()) {
                checkForUpdate();
            }
        }, 1, 4, TimeUnit.HOURS); // Initial delay 1, then every 4 hours
    }

    private void checkForUpdate() {

        if (!firstRun) {
            if (Easybot.instance.getConfig().getBoolean("service.update_notify", true)) {
                return;
            }
        }

        Easybot.instance.getLogger().info("检查更新中");
        GetNewVersionResultPacket newVersion = null;
        try {
            if(Easybot.getClient().isReady()){
                newVersion = Easybot.getClient().getNewVersion();
            }
        } catch (Exception ignored) {
        }

        firstRun = false;

        if (newVersion == null || newVersion.getPublishTime().equals("")) {
            Easybot.instance.getLogger().info("没有可用更新");
            return;
        }

        if (!newVersion.getVersionName().equals(Easybot.instance.getDescription().getVersion())) {
            Easybot.instance.getLogger().warning("检查到更新");
            Easybot.instance.getLogger().warning("EasyBot服务器插件v" + newVersion.getVersionName() + "已于" + newVersion.getPublishTime() + "发布");
            Easybot.instance.getLogger().warning("更新日志: " + newVersion.getUpdateLog());
            Easybot.instance.getLogger().warning("请前往: " + newVersion.getDownloadUrl() + " 下载更新!!");
        } else {
            Easybot.instance.getLogger().info("已是最新版本");
        }
    }
}