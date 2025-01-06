package com.springwater.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnknownSegment implements Segment{
    @SerializedName("raw_type")
    private String rawType;
    @SerializedName("raw_content")
    private String rawContent;
    @Override
    public String getRawText() {
        return rawContent;
    }

    @Override
    public String getText() {
        return "[未知]";
    }
}
