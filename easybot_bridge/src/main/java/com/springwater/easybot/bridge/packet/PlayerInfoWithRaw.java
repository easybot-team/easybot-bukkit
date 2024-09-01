package com.springwater.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerInfoWithRaw
{
    @SerializedName("ip")
    public String ip;
    @SerializedName("player_name")
    public String name;
    @SerializedName("player_uuid")
    public String uuid;
    @SerializedName("player_name_raw")
    public String nameRaw;
}
