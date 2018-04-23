package com.example.john.mimicvideo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.CameraActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.ShowVideoContentActivity;
import com.example.john.mimicvideo.TestVideoActivity;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.VideoSample;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

/**
 * Created by john on 2018/3/21.
 */

public class VideoSampleAdapter extends RecyclerView.Adapter<VideoSampleAdapter.ViewHolder>{
    Context context;
    List<VideoSample>videoSampleList;

    public VideoSampleAdapter(Context context, List<VideoSample>videoSampleList) {
        this.context = context;
        this.videoSampleList = videoSampleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video_sample, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Glide.with(context)
                .load(videoSampleList.get(position).url)
                .into( holder.videoSampleThumbnailImg);

        holder.videoSampleTitleTxt.setText(videoSampleList.get(position).title);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("video_sample_id", videoSampleList.get(position).id);
                intent.putExtra("video_sample_url", videoSampleList.get(position).url);
                intent.setClass(context, TestVideoActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoSampleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView videoSampleThumbnailImg;
        TextView videoSampleTitleTxt;
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            videoSampleThumbnailImg = itemView.findViewById(R.id.videoSampleThumbnailImg);
            videoSampleTitleTxt = itemView.findViewById(R.id.videoSampleTitleTxt);
        }
    }

    public void setVideoSampleList(List<VideoSample>videoSampleList){
        this.videoSampleList = videoSampleList;
        notifyDataSetChanged();
    }
}


