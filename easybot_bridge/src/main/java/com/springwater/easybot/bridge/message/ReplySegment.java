package com.springwater.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplySegment implements Segment{
    @SerializedName("id")
    private String id;
    @Override
    public String getRawText() {
        return "[回复某条消息]";
    }

    @Override
    public String getText() {
        return "[回复某条消息]";
    }
}
