package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerLoginResultPacket extends PacketWithCallBackId{
    @SerializedName("kick")
    private Boolean kick;
    @SerializedName("kick_message")
    private String kickMessage;
}
