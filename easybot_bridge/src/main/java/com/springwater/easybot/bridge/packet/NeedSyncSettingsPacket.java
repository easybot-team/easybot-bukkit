package com.springwater.easybot.bridge.packet;

public class NeedSyncSettingsPacket extends PacketWithCallBackId{
    public NeedSyncSettingsPacket(){
        setOperation("NEED_SYNC_SETTING");
    }
}
