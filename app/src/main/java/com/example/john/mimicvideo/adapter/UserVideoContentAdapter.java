package com.example.john.mimicvideo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.EditVideoContentActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.ShowVideoContentActivity;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.JSONParser;
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

import java.util.HashMap;
import java.util.List;

/**
 * Created by john on 2018/3/26.
 */

public class UserVideoContentAdapter extends RecyclerView.Adapter<UserVideoContentAdapter.ViewHolder> {
    Context context;
    List<VideoContent> videoContentList;
    int MY_VIDEO_CONTENT = 0;
    int SUBSCRIBE_VIDEO_CONTENT = 1;
    int videoType = 0;

    public UserVideoContentAdapter(Context context, List<VideoContent>videoContentList) {
        this.context = context;
        this.videoContentList = videoContentList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user_video_content, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Glide.with(context)
                .load(videoContentList.get(position).url)
                .into(holder.userVideoContentThumbnailImg);

        holder.userVideoContentTitleTxt.setText(videoContentList.get(position).title);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoType == MY_VIDEO_CONTENT){
                    Intent intent = new Intent();
                    intent.setClass(context, EditVideoContentActivity.class);
                    intent.putExtra("videoContent", videoContentList.get(position));
                    intent.putExtra("videoContentId", videoContentList.get(position).id);
                    ((Activity)context).startActivityForResult(intent, 0);
                }else{
                    Intent intent = new Intent();
                    intent.setClass(context, ShowVideoContentActivity.class);
                    intent.putExtra("videoContentId", videoContentList.get(position).id);
                    context.startActivity(intent);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return videoContentList.size();
    }

    public void setVideoContentList(List<VideoContent> videoContentList, int videoType){
        this.videoContentList = videoContentList;
        this.videoType = videoType;
        notifyDataSetChanged();
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView userVideoContentThumbnailImg;
        TextView userVideoContentTitleTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            userVideoContentThumbnailImg = itemView.findViewById(R.id.userVideoContentThumbnailImg);
            userVideoContentTitleTxt = itemView.findViewById(R.id.userVideoContentTitleTxt);
        }
    }
}
