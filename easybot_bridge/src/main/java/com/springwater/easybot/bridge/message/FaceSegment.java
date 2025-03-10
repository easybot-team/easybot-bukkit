package com.springwater.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaceSegment implements Segment {
    @SerializedName("face_id")
    public String id = "";
    @SerializedName("display_name")
    public String displayName = "";
    @Override
    public String getRawText() {
        return "[" + displayName + "]";
    }

    @Override
    public String getText() {
        return "[" + displayName + "]";
    }
}
