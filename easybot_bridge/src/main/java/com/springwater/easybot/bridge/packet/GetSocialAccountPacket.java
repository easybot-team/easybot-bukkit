package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSocialAccountPacket extends PacketWithCallBackId {
    @SerializedName("player_name")
    private String playerName;
    public GetSocialAccountPacket(){
        setOperation("GET_SOCIAL_ACCOUNT");
    }
}

