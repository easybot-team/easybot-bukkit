package com.springwater.easybot.hook.impl;

import org.bukkit.Server;
import org.bukkit.event.Event;

public class BukkitEventHandler {
    public boolean onBukkitEvent(Server server, Event event){
        //server.getLogger().info("BukkitEvent: " + event.getEventName());
        return false;
    }
}
