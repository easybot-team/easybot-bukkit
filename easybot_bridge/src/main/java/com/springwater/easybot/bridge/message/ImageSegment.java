package com.springwater.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageSegment implements Segment{
    @SerializedName("url")
    private String url;

    @Override
    public String getRawText() {
        return "[IMG=" + url + "]";
    }

    @Override
    public String getText() {
        return "[图片]";
    }
}
