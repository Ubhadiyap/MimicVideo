package com.shou.john.mimicvideo.model;

import java.io.Serializable;

/**
 * Created by john on 2018/3/24.
 */

public class Comment implements Serializable {
    public String id;
    public User owner;
    public String content;
}
