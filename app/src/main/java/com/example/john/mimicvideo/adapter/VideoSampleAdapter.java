package com.example.john.mimicvideo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.LoadingActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.model.VideoSample;

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
                intent.putExtra("videoSampleId", videoSampleList.get(position).id);
                intent.putExtra("video_sample_url", videoSampleList.get(position).url);
                intent.setClass(context, LoadingActivity.class);
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


