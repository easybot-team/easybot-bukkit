package com.springwater.easybot.hook.impl;

import com.springwater.easybot.Easybot;
import org.bukkit.Server;
import org.bukkit.event.Event;

public class SendMessageHandler {
    public boolean onSendMessage(Object server, Object event){
        Easybot.instance.getLogger().info("callback: " + event.toString());
        return false;
    }
}
