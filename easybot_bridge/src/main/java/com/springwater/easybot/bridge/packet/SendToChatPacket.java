package com.springwater.easybot.bridge.packet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendToChatPacket extends PacketWithCallBackId{
    @SerializedName("text")
    private String text;
    @SerializedName("extra")
    private JsonArray extra;
}
