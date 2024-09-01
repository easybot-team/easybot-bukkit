package com.springwater.easybot.bridge;

public enum OpCode {
    Hello(0),
    Identify(1),
    HeartBeat(2),
    IdentifySuccess(3),
    Packet(4),
    CallBack(5);

    private final int value;

    OpCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static OpCode fromValue(int value) {
        for (OpCode opCode : OpCode.values()) {
            if (opCode.getValue() == value) {
                return opCode;
            }
        }
        return null;
    }
}
