package com.springwater.easybot.event;

import com.springwater.easybot.Easybot;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.utils.BridgeUtils;
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

public class PlayerDeathSyncEvents implements Listener {
    public String getKiller(Player player) {
        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
            if(damager instanceof Arrow){
                Arrow arrow = (Arrow) damager;
                if(arrow.getShooter() instanceof Entity){
                    return ((Entity) arrow.getShooter()).getName();
                }
                return "箭";
            }
            return damager != null ? damager.getName() : "一股神秘的力量";
        } else if (lastDamageCause instanceof EntityDamageByBlockEvent) {
            Block damager = ((EntityDamageByBlockEvent) lastDamageCause).getDamager();
            return damager != null ? damager.getState().getData().getItemType().name() : "一股神秘的力量";
        } else {
            return "一股神秘的力量";
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerInfoWithRaw playerInfo = BridgeUtils.buildPlayerInfoFull(event.getEntity());
        String deathMessage = event.getDeathMessage();
        if(deathMessage == null){
            deathMessage = event.getEntity().getName() + "  died";
        }
        final String message = deathMessage;
        String killer = getKiller(event.getEntity());
        new Thread(() -> {
            Easybot
                    .getClient()
                    .syncDeathMessage(playerInfo, message, killer);
        }, "EasyBotThread-SyncPlayerDeath").start();
    }
}
