package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetBindInfoResultPacket extends PacketWithCallBackId{
    @SerializedName("name")
    private String name;
    @SerializedName("platform")
    private String platform;
    @SerializedName("bind_names")
    private String bindName;
    @SerializedName("id")
    private String id;
}
