package com.shou.john.mimicvideo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.R;
import com.shou.john.mimicvideo.ShowVideoContentActivity;
import com.shou.john.mimicvideo.model.VideoContent;

import java.util.List;

/**
 * Created by john on 2018/4/15.
 */

public class OtherUserVideoContentAdapter extends RecyclerView.Adapter<OtherUserVideoContentAdapter.ViewHolder>{
    Context context;
    List<VideoContent> videoContentList;

    public OtherUserVideoContentAdapter(Context context, List<VideoContent>videoContentList) {
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
                Intent intent = new Intent();
                intent.setClass(context, ShowVideoContentActivity.class);
                intent.putExtra("videoContentId", videoContentList.get(position).id);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return videoContentList.size();
    }

    public void setVideoContentList(List<VideoContent> videoContentList){
        this.videoContentList = videoContentList;
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
