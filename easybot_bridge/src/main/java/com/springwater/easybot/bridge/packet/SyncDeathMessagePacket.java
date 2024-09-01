package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncDeathMessagePacket extends PacketWithCallBackId
{
    @SerializedName("player")
    private PlayerInfoWithRaw player;
    @SerializedName("raw")
    private String raw;
    @SerializedName("killer")
    private String killer;
    
    public SyncDeathMessagePacket(){
        setOperation("SYNC_DEATH_MESSAGE");
    }
}
