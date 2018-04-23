package com.example.john.mimicvideo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.model.VideoContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/3/26.
 */

public class ReportDescriptionAdapter extends RecyclerView.Adapter<ReportDescriptionAdapter.ViewHolder>{
    Context context;
    String[] reportDescriptionArray = {"言論不恰當", "不想看到", "影片內容"};

    public ReportDescriptionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_report, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.reportDescriptionTxt.setText(reportDescriptionArray[position]);
        holder.reportDescriptionClickImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reportDescriptionArray.length;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView reportDescriptionTxt;
        ImageView reportDescriptionClickImg;
        public ViewHolder(View itemView) {
            super(itemView);
            reportDescriptionTxt = itemView.findViewById(R.id.reportDescriptionTxt);
            reportDescriptionClickImg = itemView.findViewById(R.id.reportDescriptionClickImg);
        }
    }
}
