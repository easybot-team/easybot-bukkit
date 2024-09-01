package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncMessagePacket extends PacketWithCallBackId
{
    @SerializedName("player")
    private PlayerInfoWithRaw player;
    @SerializedName("message")
    private String message;
    @SerializedName("use_command")
    private boolean useCommand;
    
    public SyncMessagePacket(){
        setOperation("SYNC_MESSAGE");
    }
}
