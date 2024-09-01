package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SetSocialAccountPacket extends PacketWithCallBackId{
    @SerializedName("player_name")
    private String playerName;

    public SetSocialAccountPacket() {
        setOperation("GET_SOCIAL_ACCOUNT");
    }
}

