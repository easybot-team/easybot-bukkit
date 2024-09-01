package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetNewVersionResultPacket extends PacketWithCallBackId{
    @SerializedName("version_name")
    private String versionName;
    @SerializedName("download_url")
    private String downloadUrl;
    @SerializedName("publish_time")
    private String publishTime;
    @SerializedName("update_log")
    private String updateLog;
}
