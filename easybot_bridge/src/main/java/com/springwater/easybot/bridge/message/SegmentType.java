package com.springwater.easybot.bridge.message;

public enum SegmentType {

    UNKNOWN(1),
    TEXT(2),
    IMAGE(3),
    AT(4),
    FILE(5),
    REPLY(6),
    FACE(7);

    private int type;

    SegmentType(int type) {
        this.type = type;
    }

    public static SegmentType getSegmentType(int type) {
        for (SegmentType segmentType : SegmentType.values()) {
            if (segmentType.getType() == type) {
                return segmentType;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }
}
