package com.shou.john.mimicvideo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.john.mimicvideo.R;

/**
 * Created by john on 2018/3/26.
 */

public class ReportDescriptionAdapter extends RecyclerView.Adapter<ReportDescriptionAdapter.ViewHolder>{
    Context context;
    String[] reportDescriptionArray = {"涉及暴力，威脅", "不想看到", "影片內容猥褻"};
    int selectedPosition = -1;
    Button reportSubmitBtn;

    public ReportDescriptionAdapter(Context context, Button reportSubmitBtn) {
        this.context = context;
        this.reportSubmitBtn = reportSubmitBtn;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(selectedPosition == position){
            holder.reportDescriptionClickImg.setVisibility(View.VISIBLE);
        }else{
            holder.reportDescriptionClickImg.setVisibility(View.INVISIBLE);
        }

        if(selectedPosition != -1){
            reportSubmitBtn.setBackgroundColor(context.getResources().getColor(R.color.com_facebook_blue));
            reportSubmitBtn.setEnabled(true);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = position;
                notifyDataSetChanged();
            }
        });

        holder.reportDescriptionTxt.setText(reportDescriptionArray[position]);
    }

    @Override
    public int getItemCount() {
        return reportDescriptionArray.length;
    }

    public String getSelectedWord(){
        if(selectedPosition != -1){
            return reportDescriptionArray[selectedPosition];
        }else{
            return null;
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView reportDescriptionTxt;
        ImageView reportDescriptionClickImg;
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            reportDescriptionTxt = itemView.findViewById(R.id.reportDescriptionTxt);
            reportDescriptionClickImg = itemView.findViewById(R.id.reportDescriptionClickImg);
        }
    }
}
