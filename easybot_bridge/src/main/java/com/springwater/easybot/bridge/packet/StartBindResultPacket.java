package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StartBindResultPacket extends PacketWithCallBackId{
    @SerializedName("code")
    private String code;
    @SerializedName("time")
    private String time;
}
