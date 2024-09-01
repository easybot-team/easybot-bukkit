package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendToChatPacket extends PacketWithCallBackId{
    @SerializedName("text")
    private String text;
}
