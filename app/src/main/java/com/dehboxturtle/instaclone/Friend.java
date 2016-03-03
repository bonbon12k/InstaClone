package com.dehboxturtle.instaclone;

/**
 * Created by smartin1 on 2/23/16.
 */
public class Friend {

    private String display_name;
    private String avatar;
    private String uid;
    private int image_count;

    public Friend() {}

    public String getDisplay_name() {
        return display_name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getImage_count() {
        return image_count;
    }
}
