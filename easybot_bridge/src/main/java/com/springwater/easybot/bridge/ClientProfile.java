package com.springwater.easybot.bridge;

import lombok.Getter;
import lombok.Setter;

public class ClientProfile {
    @Getter
    @Setter
    private static String pluginVersion;

    @Getter
    @Setter
    private static String serverDescription;

    @Getter
    @Setter
    private static boolean isCommandSupported;

    @Getter
    @Setter
    private static boolean isPapiSupported;

    @Getter
    @Setter
    private static int syncMessageMode;

    @Getter
    @Setter
    private static int syncMessageMoney;

}
