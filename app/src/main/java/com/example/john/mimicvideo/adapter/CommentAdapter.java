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
import com.example.john.mimicvideo.OtherProfileActivity;
import com.example.john.mimicvideo.ProfileActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.ShowVideoContentActivity;
import com.example.john.mimicvideo.model.Comment;
import com.example.john.mimicvideo.utils.JSONParser;

import java.util.List;

/**
 * Created by john on 2018/3/25.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    Context context;
    List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment>commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Glide.with(context)
                .load(commentList.get(position).owner.profile)
                .into(holder.commentProfileImg);
        holder.commentNameTxt.setText(commentList.get(position).owner.name);
        holder.commentContentTxt.setText(commentList.get(position).content);

        holder.commentProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, OtherProfileActivity.class);
                intent.putExtra("id",commentList.get(position).owner.id);
                intent.putExtra("name", commentList.get(position).owner.name);
                intent.putExtra("profile", commentList.get(position).owner.profile);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView commentProfileImg;
        TextView commentNameTxt;
        TextView commentContentTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            commentProfileImg = itemView.findViewById(R.id.commentProfileImg);
            commentNameTxt = itemView.findViewById(R.id.commentNameTxt);
            commentContentTxt = itemView.findViewById(R.id.commentContentTxt);
        }
    }

    public void addNewComment(Comment comment){
        commentList.add(comment);
    }
}
