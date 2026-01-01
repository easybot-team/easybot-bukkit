package com.springwater.easybot.utils;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

public class FakePlayerUtils {
    private static boolean installed = false;
    private static FakeplayerList list;
    private static volatile Map<String, Object> cachedPlayersMap;
    public static boolean isInstalled() {
        try {
            // 找 io.github.hello09x.fakeplayer.core.Main
            Class.forName("io.github.hello09x.fakeplayer.core.Main");
            installed = true;
        }catch (Exception ignored) {
            installed = false;
        }
        return installed;
    }

    @SneakyThrows
    private static void init(){
        if(list != null) return;
        list = Main.getInjector().getInstance(FakeplayerList.class);
        // 解释一下为什么一定要通过这个字典获取
        // https://github.com/tanyaofei/minecraft-fakeplayer/issues/191
        Field field = list.getClass().getDeclaredField("playersByName");
        field.setAccessible(true);
        //noinspection unchecked
        cachedPlayersMap = (Map<String, Object>) field.get(list);
    }

    public static boolean isNotFake(@NotNull Player player) {
        if (!installed) return true;
        init();
        return !cachedPlayersMap.containsKey(player.getName());
    }

    public static boolean isFake(@NotNull Player player) {
        if (!installed) return false;
        init();
        return cachedPlayersMap.containsKey(player.getName());
    }

    public static boolean isFake(@NotNull String name) {
        if (!installed) return false;
        init();
        return cachedPlayersMap.containsKey(name);
    }
}