package com.springwater.easybot.bridge;

import com.springwater.easybot.bridge.model.ServerInfo;

public interface BridgeBehavior {
    String runCommand(String playerName, String command, boolean enablePapi);
    String papiQuery(String playerName, String query);
    ServerInfo getInfo();
    void SyncToChat(String message);
    void BindSuccessBroadcast(String playerName,String accountId, String accountName);
    void KickPlayer(String plauer, String kickMessage);
}
