package com.example.john.mimicvideo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.CameraActivity;
import com.example.john.mimicvideo.CommentActivity;
import com.example.john.mimicvideo.LoginActivity;
import com.example.john.mimicvideo.OtherProfileActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.SameVideoContentActivity;
import com.example.john.mimicvideo.ShareActivity;
import com.example.john.mimicvideo.ShowVideoContentActivity;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/4/15.
 */

public class MainVideoContentAdapter extends RecyclerView.Adapter<MainVideoContentAdapter.ViewHolder>  {
    Context context;
    List<VideoContent>mainVideoContentList;
    private JSONParser jsonParser = new JSONParser();
    private SharePreferenceDB sharePreferenceDB;
    private int user_id;

    public MainVideoContentAdapter(Context context, List<VideoContent>mainVideoContentList) {
        this.context = context;
        this.mainVideoContentList = mainVideoContentList;
        sharePreferenceDB = new SharePreferenceDB(context);
        user_id = sharePreferenceDB.getInt("id");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pager_row_video_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final VideoContent videoContent = mainVideoContentList.get(position);
//        showVideo(position);
        holder.ownerNameTxt.setText(videoContent.owner.name);
        holder.videoContentTitleTxt.setText(videoContent.title);

        holder.commentAmountTxt.setText(String.valueOf(videoContent.commentAmount));
        holder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));

        Glide.with(context)
                .load(videoContent.owner.profile)
                .into(holder.ownerProfileImg);

        holder.openCommentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("video_content_id", videoContent.id);
                intent.setClass(context, CommentActivity.class);
                context.startActivity(intent);
            }
        });

        holder.openVideoSampleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, CameraActivity.class);
                intent.putExtra("video_sample_id", videoContent.videoSampleId);
                intent.putExtra("video_sample_url", videoContent.videoSampleUrl);
                context.startActivity(intent);
            }
        });

        holder.openSameVideoContentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("video_sample_id", videoContent.videoSampleId);
                intent.setClass(context, SameVideoContentActivity.class);
                context.startActivity(intent);
            }
        });

        for(int i = 0; i < videoContent.likeList.size(); i++){
            if(videoContent.likeList.get(i).user_id == user_id){
                if(videoContent.likeList.get(i).is_click == 1){
                    holder.giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.com_facebook_button_background_color));
                }else{
                    holder.giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.white));
                }
            }else{
                holder.giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }
        holder.giveLikeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharePreferenceDB.getInt("id") != 0){
                    if(((ColorDrawable)view.getBackground()).getColor() == context.getResources().getColor(R.color.com_facebook_button_background_color)){
                        view.setBackgroundColor(context.getResources().getColor(R.color.white));
                        new UpdateLikeAmount(user_id, videoContent.id, 0).execute();
                        mainVideoContentList.get(position).likeAmount =  mainVideoContentList.get(position).likeAmount - 1;
                        holder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));
                    }else{
                        view.setBackgroundColor(context.getResources().getColor(R.color.com_facebook_button_background_color));
                        new UpdateLikeAmount(user_id, videoContent.id, 1).execute();
                        mainVideoContentList.get(position).likeAmount = mainVideoContentList.get(position).likeAmount + 1;
                        holder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setClass(context, LoginActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        holder.ownerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, OtherProfileActivity.class);
                intent.putExtra("id", mainVideoContentList.get(position).owner.id);
                intent.putExtra("name", mainVideoContentList.get(position).owner.name);
                intent.putExtra("profile", mainVideoContentList.get(position).owner.profile);
                context.startActivity(intent);
            }
        });

        holder.shareTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, ShareActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mainVideoContentList.size();
    }

    public void setMainVideoContentList(List<VideoContent> mainVideoContentList){
        this.mainVideoContentList = mainVideoContentList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        public SimpleExoPlayerView videoContentPlayerView;
        private ImageView ownerProfileImg;
        public TextView ownerNameTxt;
        public ImageView openCommentImg;
        public ImageView openVideoSampleImg;
        public ImageView openSameVideoContentImg;
        public ImageView giveLikeImg;
        public TextView shareTxt;
        public TextView videoContentTitleTxt;
        public LinearLayout ownerLayout;
        public TextView commentAmountTxt;
        public TextView likeAmountTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            videoContentPlayerView = itemView.findViewById(R.id.videoContentPlayerView);
            ownerProfileImg = itemView.findViewById(R.id.ownerProfileImg);
            ownerNameTxt = itemView.findViewById(R.id.ownerNameTxt);
            openCommentImg = itemView.findViewById(R.id.openCommentImg);
            openVideoSampleImg = itemView.findViewById(R.id.openVideoSampleImg);
            openSameVideoContentImg = itemView.findViewById(R.id.openSameVideoContentImg);
            giveLikeImg = itemView.findViewById(R.id.giveLikeImg);
            videoContentTitleTxt = itemView.findViewById(R.id.videoContentTitleTxt);
            ownerLayout = itemView.findViewById(R.id.ownerLayout);
            shareTxt = itemView.findViewById(R.id.shareTxt);
            shareTxt.setTypeface(ApplicationService.getFont());
            shareTxt.setText(R.string.fa_share_square_o);
            commentAmountTxt = itemView.findViewById(R.id.commentAmountTxt);
            likeAmountTxt = itemView.findViewById(R.id.likeAmountTxt);
        }
    }

    class UpdateLikeAmount extends AsyncTask<String, String, String> {
        int user_id;
        int video_content_id;
        int is_click;

        public UpdateLikeAmount(int user_id, int video_content_id, int is_click){
            this.user_id = user_id;
            this.video_content_id = video_content_id;
            this.is_click =is_click;

        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
            params.add(new BasicNameValuePair("video_content_id", String.valueOf(video_content_id)));
            params.add(new BasicNameValuePair("is_click", String.valueOf(is_click)));
            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest("http://1.34.63.239/give_video_content_like.php", "POST", params);

            if(json != null){
                // Check your log cat for JSON reponse
                Log.d("Video data: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

        }

    }

}
