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
}
