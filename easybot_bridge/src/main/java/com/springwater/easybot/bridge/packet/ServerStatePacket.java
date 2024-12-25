package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import com.springwater.easybot.bridge.OpCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatePacket extends PacketWithCallBackId {
    @SerializedName("token")
    private String token;
    @SerializedName("players")
    private String players;
    public ServerStatePacket() {
        setOpCode(OpCode.Packet);
        setOperation("SERVER_STATE_CHANGED");
    }
}

