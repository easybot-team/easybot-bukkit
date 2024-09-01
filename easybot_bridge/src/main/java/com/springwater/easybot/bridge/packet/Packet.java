package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import com.springwater.easybot.bridge.OpCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Packet {
    @SerializedName("op")
    private OpCode opCode;
}
