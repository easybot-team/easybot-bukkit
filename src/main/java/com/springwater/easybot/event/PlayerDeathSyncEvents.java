package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
import com.springwater.easybot.utils.FakePlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

public class PlayerDeathSyncEvents implements Listener {
    
    private final boolean hasModernMessageApi;

    public PlayerDeathSyncEvents(){
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
            Block damager = ((EntityDamageByBlockEvent) lastDamageCause).getDamager();
            return damager != null ? damager.getState().getType().name() : "一股神秘的力量";
        } else {
            return "一股神秘的力量";
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (Easybot.instance.getConfig().getBoolean("skip_options.skip_death")) return;
        if (FakePlayerUtils.isFake(event.getEntity())) return;
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getEntity());
        String deathMessage;
        
        if(hasModernMessageApi) {
            Component component = event.deathMessage();
            deathMessage = LegacyComponentSerializer.legacySection().serializeOrNull(component);
        }else{
            //noinspection deprecation
            deathMessage = event.getDeathMessage();
        }

        if (deathMessage == null) {
            deathMessage = event.getEntity().getName() + "  died";
        }
        final String message = deathMessage;
        String killer = getKiller(event.getEntity());
        new Thread(() -> Easybot.getClient().syncDeathMessage(playerInfo, message, killer), "EasyBotThread-SyncPlayerDeath").start();
    }
}
