package com.springwater.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class AtSegment implements Segment {
    @SerializedName("at_user_name")
    private String atUserName;
    @SerializedName("at_user_id")
    private String atUserId;
    @SerializedName("at_player_names")
    private String[] atPlayerNames;

    @Override
    public String getRawText() {
        return getText();
    }

    @Override
    public String getText() {
        if(Objects.equals(atUserId, "0")){
            return "@全体成员";
        }

        if (atPlayerNames.length != 0) {
            return "@" + atPlayerNames[0];
        }
        return "@" + atUserId;
    }
}
