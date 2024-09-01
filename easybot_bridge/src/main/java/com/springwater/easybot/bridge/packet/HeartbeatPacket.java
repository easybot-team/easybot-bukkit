package com.springwater.easybot.bridge.packet;

import com.springwater.easybot.bridge.OpCode;

public class HeartbeatPacket extends Packet {
    public HeartbeatPacket(){
        setOpCode(OpCode.HeartBeat);
    }
}
