package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelloPacket extends Packet{
    @SerializedName("version")
    private String version;

    @SerializedName("system")
    private String systemName;

    @SerializedName("dotnet")
    private String dotnetVersion;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("token")
    private String token;

    @SerializedName("interval")
    private int interval;
}
