package com.springwater.easybot.bridge.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerInfo {
    @SerializedName("server_name")
    private String serverName;

    @SerializedName("server_version")
    private String serverVersion;

    @SerializedName("plugin_version")
    private String pluginVersion;

    @SerializedName("is_papi_supported")
    private boolean isPapiSupported;

    @SerializedName("is_command_supported")
    private boolean isCommandSupported;

    @SerializedName("has_geyser")
    private boolean hasGeyser;

    @SerializedName("is_online_mode")
    private boolean isOnlineMode;
}
