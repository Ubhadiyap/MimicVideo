package com.example.john.mimicvideo.model;

import java.io.Serializable;

/**
 * Created by john on 2018/4/8.
 */

public class Like implements Serializable {
    public int id;
    public int user_id;
    public int video_content_id;
    public int is_click;

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Like))
            return false;
        Like other = (Like) obj;
        System.out.println("wire " + (user_id != 0 && user_id == other.user_id));
        return user_id != 0 && user_id == other.user_id;//Compare Id if null falseF
    }
}
