package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import com.springwater.easybot.bridge.OpCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataRecordPacket extends PacketWithCallBackId {
    @SerializedName("data")
    private String data;
    @SerializedName("name")
    private String name;
    @SerializedName("token")
    private String token;
    @SerializedName("type")
    private RecordTypeEnum type;

    public DataRecordPacket() {
        setOpCode(OpCode.Packet);
        setOperation("DATA_RECORD");
    }
}
