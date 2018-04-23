package com.example.john.mimicvideo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.ShowVideoContentActivity;
import com.example.john.mimicvideo.model.VideoContent;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.List;

/**
 * Created by john on 2018/4/4.
 */

public class VideoFrameAdapter extends RecyclerView.Adapter<VideoFrameAdapter.ViewHolder> {
    Context context;
    List<Bitmap> videoFrameList;
    SimpleExoPlayerView videoContentPlayerView;


    public VideoFrameAdapter(Context context, List<Bitmap>videoFrameList, SimpleExoPlayerView videoContentPlayerView) {
        this.context = context;
        this.videoFrameList = videoFrameList;
        this.videoContentPlayerView = videoContentPlayerView;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video_frame, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.videoFrameImg.setImageBitmap(videoFrameList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoContentPlayerView.getPlayer().seekTo((position * 5) * 1000);
                videoContentPlayerView.getPlayer().setPlayWhenReady(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoFrameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView videoFrameImg;
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            videoFrameImg = itemView.findViewById(R.id.videoFrameImg);
        }
    }
}
