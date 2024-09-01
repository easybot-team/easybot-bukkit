package com.springwater.easybot.bridge.packet;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetNewVersionPacket extends PacketWithCallBackId{
    public GetNewVersionPacket() {
        setOperation("GET_NEW_VERSION");
    }
}
