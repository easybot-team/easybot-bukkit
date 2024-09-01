package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentifySuccessPacket extends Packet{
    @SerializedName("server_name")
    private String serverName;
}
