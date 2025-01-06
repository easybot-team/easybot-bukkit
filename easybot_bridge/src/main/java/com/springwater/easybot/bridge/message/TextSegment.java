package com.springwater.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextSegment implements Segment{
    @SerializedName("text")
    private String text;
    @Override
    public String getRawText() {
        return text;
    }
}
