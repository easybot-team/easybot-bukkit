package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSyncSettingsPacket extends PacketWithCallBackId{
    @SerializedName("sync_mode")
    private int syncMode;
    @SerializedName("sync_money")
    private int syncMoney;
}

