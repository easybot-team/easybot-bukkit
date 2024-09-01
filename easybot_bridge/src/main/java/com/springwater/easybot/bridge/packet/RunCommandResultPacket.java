package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunCommandResultPacket {
    @SerializedName("text")
    private String text;
    @SerializedName("success")
    private boolean success;
}
