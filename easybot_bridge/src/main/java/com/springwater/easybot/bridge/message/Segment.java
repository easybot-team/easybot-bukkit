package com.springwater.easybot.bridge.message;

public interface Segment {
    String getRawText();
    String getText();

    static Class<? extends Segment> getSegmentClass(SegmentType type) {
        switch (type) {
            case UNKNOWN: return UnknownSegment.class;
            case TEXT:    return TextSegment.class;
            case IMAGE:   return ImageSegment.class;
            case AT:      return AtSegment.class;
            case FILE:    return FileSegment.class;
            case REPLY:   return ReplySegment.class;
            default:      return null;
        }
    }
}
