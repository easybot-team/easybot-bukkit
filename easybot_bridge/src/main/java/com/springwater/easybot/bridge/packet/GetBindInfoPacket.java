package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetBindInfoPacket extends PacketWithCallBackId{
    @SerializedName("player_name")
    private String playerName;

    public GetBindInfoPacket() {
        setOperation("GET_BIND_INFO");
    }
}

