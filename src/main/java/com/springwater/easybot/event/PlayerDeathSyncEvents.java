package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.i18n.I18n;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.BukkitUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.reflect.Method;
import java.util.Locale;

public class PlayerDeathSyncEvents implements Listener {

    private final boolean hasModernMessageApi;

    public PlayerDeathSyncEvents() {
        boolean modernMessageApi = false;
        try {
            PlayerDeathEvent.class.getMethod("deathMessage");
            modernMessageApi = true;
        } catch (NoSuchMethodException | NoClassDefFoundError ignored) {
        }
        this.hasModernMessageApi = modernMessageApi;
    }

    public String getKiller(Player player) {
        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
            try {
                try {
                    Component customName = damager.customName();
                    if (customName != null) { // 这应该是命名牌?
                        return LegacyComponentSerializer.legacySection().serializeOrNull(
                                I18n.render(customName, Locale.CHINESE)
                        );
                    }
                } catch (Throwable ignored) {
                    try {
                        @SuppressWarnings("deprecation")
                        String legacyCustomName = damager.getCustomName();
                        if (legacyCustomName != null) {
                            return legacyCustomName;
                        }
                    } catch (Throwable ignored2) {
                    }
                }

                Component translated = I18n.render(damager.name(), Locale.CHINESE);
                return LegacyComponentSerializer.legacySection().serializeOrNull(translated);
            } catch (Throwable ignored) {
            }


            // ========================
            // Legacy
            // ========================

            if (damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager;
                if (arrow.getShooter() instanceof Entity) {
                    return ((Entity) arrow.getShooter()).getName();
                }
                return "箭";
            }
            //noinspection ConstantValue
            return damager != null ? damager.getName() : "一股神秘的力量";
        } else if (lastDamageCause instanceof EntityDamageByBlockEvent) {
            // Folia：方块可能属于其它区域线程，或所在区块尚未加载，
            // 此时读方块状态会走 ServerChunkCache#syncLoad 无限等待，直接卡死区域线程（issue #126）。
            // 所以只有在「区块已加载 + 区域归属当前线程」时才读方块，否则退回按伤害类型描述。
            Block damager = ((EntityDamageByBlockEvent) lastDamageCause).getDamager();
            if (damager != null && isBlockSafeToRead(damager)) {
                try {
                    return damager.getType().name();
                } catch (Throwable ignored) {
                    // 落到下面的兜底
                }
            }
            return getBlockDamageFallback(lastDamageCause.getCause());
        } else {
            return "一股神秘的力量";
        }
    }

    /**
     * 在事件回调里读该方块状态是否安全：
     * 1) 所在区块必须已加载（{@code isChunkLoaded} 只是查询，不会触发加载）；
     * 2) Folia 下该区块必须由当前区域线程拥有，否则跨区域读取同样会阻塞。
     */
    private static boolean isBlockSafeToRead(Block block) {
        try {
            World world = block.getWorld();
            int chunkX = block.getX() >> 4;
            int chunkZ = block.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) return false;
            return BukkitUtils.isOwnedByCurrentRegion(world, chunkX, chunkZ);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * 读不到方块时的兜底名称：只看伤害类型，不触碰任何区块数据。
     * 这里用字符串判断而不是 switch(枚举常量)：低版本服务端可能没有 CAMPFIRE / SOUL_CAMPFIRE / FREEZE，
     * 而 switch 枚举常量会在类初始化时抛 NoSuchFieldError。
     */
    private static String getBlockDamageFallback(EntityDamageEvent.DamageCause cause) {
        if (cause == null) return "方块";
        switch (cause.name()) {
            case "CONTACT":
                return "仙人掌";
            case "LAVA":
                return "岩浆";
            case "FIRE":
            case "FIRE_TICK":
                return "火焰";
            case "HOT_FLOOR":
                return "岩浆块";
            case "CAMPFIRE":
                return "营火";
            case "SOUL_CAMPFIRE":
                return "灵魂营火";
            case "SUFFOCATION":
                return "窒息";
            case "FREEZE":
                return "冰冻";
            case "VOID":
                return "虚空";
            default:
                return "方块";
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (Easybot.instance.getConfig().getBoolean("skip_options.skip_death")) return;
        if (FakePlayerUtils.isFake(event.getEntity())) return;
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getEntity());
        String deathMessage = null;

        if (hasModernMessageApi) {
            try {
                Component component = event.deathMessage();
                if (component != null) {
                    Component translated = I18n.render(component, Locale.CHINESE);
                    deathMessage = LegacyComponentSerializer.legacySection().serializeOrNull(translated);
                }
            } catch (Throwable ignored) {
                // Adventure 接口变动（如 26.x）时不要影响死亡同步, 下面回退到旧接口
            }
        }
        if (deathMessage == null) {
            //noinspection deprecation
            deathMessage = event.getDeathMessage();
        }

        if (deathMessage == null) {
            deathMessage = event.getEntity().getName() + "  died";
        }
        final String message = deathMessage;
        final String killer = safeGetKiller(event.getEntity());

        Easybot.EXECUTOR.execute(() -> {
            Easybot
                    .getClient()
                    .syncDeathMessage(playerInfo, message, killer);
        });
    }

    /**
     * 击杀者获取整体兜底: 26.x 上名字/翻译相关接口有变动时不应该影响死亡同步
     */
    private String safeGetKiller(Player player) {
        try {
            return getKiller(player);
        } catch (Throwable ignored) {
            return "一股神秘的力量";
        }
    }
}
