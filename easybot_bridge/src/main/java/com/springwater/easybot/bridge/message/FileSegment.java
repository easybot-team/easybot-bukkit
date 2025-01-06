package com.springwater.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSegment implements Segment {
    @SerializedName("file_name")
    private String fileName;

    @SerializedName("file_md5")
    private String fileMd5;

    @SerializedName("file_size")
    private long fileSize;

    @SerializedName("file_url")
    private String fileUrl;

    @Override
    public String getRawText() {
        if (fileSize == 0) return  "[" + fileName + " / 0kb]";
        return "[" + fileName + " / " + fileSize / 1024 + "kb]";
    }

    @Override
    public String getText() {
        return "[文件]";
    }
}
