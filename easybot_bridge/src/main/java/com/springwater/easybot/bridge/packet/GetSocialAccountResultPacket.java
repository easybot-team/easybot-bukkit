package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSocialAccountResultPacket extends PacketWithCallBackId {
    @SerializedName("name")
    private String name;
    @SerializedName("time")
    private String time;
    @SerializedName("uuid")
    private String uuid;
    @SerializedName("platform")
    private String platform;
}
