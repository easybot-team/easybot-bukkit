package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncEnterExitMessagePacket extends PacketWithCallBackId
{
    @SerializedName("player")
    private PlayerInfoWithRaw player;
    @SerializedName("is_enter")
    private boolean isEnter;
    
    public SyncEnterExitMessagePacket(){
        setOperation("SYNC_ENTER_EXIT_MESSAGE");
    }
}
