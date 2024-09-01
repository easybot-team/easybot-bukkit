package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportPlayerPacket extends PacketWithCallBackId{

    @SerializedName("player_name")
    private String playerName;
    @SerializedName("player_uuid")
    private String playerUuid;
    public ReportPlayerPacket(){
        setOperation("REPORT_PLAYER");
    }
}
