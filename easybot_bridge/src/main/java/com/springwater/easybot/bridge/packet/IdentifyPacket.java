package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import com.springwater.easybot.bridge.OpCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentifyPacket {
    @SerializedName("op")
    private OpCode opCode = OpCode.Identify;
    @SerializedName("plugin_version")
    private String pluginVersion;
    @SerializedName("server_description")
    private String serverDescription;
    @SerializedName("token")
    private String token;

    public IdentifyPacket(String token) {
        this.token = token;
    }
}
