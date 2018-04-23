package com.example.john.mimicvideo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.ShowVideoContentActivity;
import com.example.john.mimicvideo.model.VideoContent;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by john on 2018/3/31.
 */

public class SearchVideoContentAdapter  extends RecyclerView.Adapter<SearchVideoContentAdapter.ViewHolder> {
    Context context;
    List<VideoContent> videoContentList;

    public SearchVideoContentAdapter(Context context, List<VideoContent>videoContentList) {
        this.context = context;
        this.videoContentList = videoContentList;
    }

    @Override
    public SearchVideoContentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_search_video_content, parent, false);
        return new SearchVideoContentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SearchVideoContentAdapter.ViewHolder holder, final int position) {

        Glide.with(context)
                .load(videoContentList.get(position).url)
                .into( holder.searchVideoContentThumbnailImg);

        holder.searchVideoContentTitleTxt.setText(videoContentList.get(position).title);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, ShowVideoContentActivity.class);
                intent.putExtra("videoContent", videoContentList.get(position));
                intent.putExtra("videoContentId", videoContentList.get(position).id);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoContentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView searchVideoContentThumbnailImg;
        TextView searchVideoContentTitleTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            searchVideoContentThumbnailImg = itemView.findViewById(R.id.searchVideoContentThumbnailImg);
            searchVideoContentTitleTxt = itemView.findViewById(R.id.searchVideoContentTitleTxt);
        }
    }

    public void resetSearch(List<VideoContent> videoContentList){
        this.videoContentList = videoContentList;
        notifyDataSetChanged();
    }
}
