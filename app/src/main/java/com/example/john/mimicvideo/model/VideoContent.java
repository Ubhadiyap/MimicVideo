package com.example.john.mimicvideo.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/3/24.
 */

public class VideoContent implements Serializable {
    public int id;
    public int videoSampleId;
    public String videoSampleUrl;
    public User owner;
    public int likeAmount = 0;
    public int commentAmount = 0;
    public String title;
    public String url;
    public List<Like> likeList = new ArrayList<>();
    public List<Comment>commentList = new ArrayList<>();
}
