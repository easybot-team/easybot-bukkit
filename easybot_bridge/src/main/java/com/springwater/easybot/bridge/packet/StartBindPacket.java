package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StartBindPacket extends PacketWithCallBackId{
    @SerializedName("player_name")
    private String playerName;

    public StartBindPacket() {
        setOperation("START_BIND");
    }
}

