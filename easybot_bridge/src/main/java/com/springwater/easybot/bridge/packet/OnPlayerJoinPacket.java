package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import com.springwater.easybot.bridge.model.PlayerInfo;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class OnPlayerJoinPacket extends PacketWithCallBackId {
    @SerializedName("player")
    private PlayerInfo playerInfo;
    public OnPlayerJoinPacket(){
        setOperation("PLAYER_JOIN");
    }
}
