package com.springwater.easybot.bridge.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerInfo {
    @SerializedName("player_name")
    private String playerName;

    @SerializedName("player_uuid")
    private String playerUuid;

    @SerializedName("ip")
    private String ip;

    @SerializedName("skin_url")
    private String skinUrl;

    @SerializedName("bedrock")
    private boolean bedrock;
}
