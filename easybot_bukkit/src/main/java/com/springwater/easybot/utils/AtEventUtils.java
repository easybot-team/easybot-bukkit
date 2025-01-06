package com.springwater.easybot.utils;

import com.springwater.easybot.Easybot;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class AtEventUtils {
    public static void at(Player player) {
        int type = Easybot.instance.getConfig().getInt("event.on_at.sound", 0);
        Easybot.instance.runTask(() ->
                player.sendTitle(
                        Easybot.instance.getConfig().getString("event.on_at.title", "").replace("&", "§"),
                        Easybot.instance.getConfig().getString("event.on_at.sub_title", "").replace("&", "§")
                )
        );
        // 启动一个新线程进行播放声音的逻辑
        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    if (type == 0) {
                        Easybot.instance.runTask(() ->
                                player.playSound(player.getLocation(), "block.anvil.land", 0.5f, 2)
                        );
                    } else if (type == 1) {
                        Easybot.instance.runTask(() ->
                                player.playSound(player.getLocation(), "block.note_block.cow_bell", 1, 1)
                        );
                    } else if (type == 2) {
                        Easybot.instance.runTask(() ->
                                player.playSound(player.getLocation(), "block.amethyst_block.resonate", 10, 1)
                        );
                    }
                    Thread.sleep(200); // 每次播放之间间隔 200 毫秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    break;
                }
            }
        }).start();
    }
}
