package com.example.john.mimicvideo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.CommentActivity;
import com.example.john.mimicvideo.FantasticCameraActivity;
import com.example.john.mimicvideo.LoginActivity;
import com.example.john.mimicvideo.OtherProfileActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.SameVideoContentActivity;
import com.example.john.mimicvideo.ShareActivity;
import com.example.john.mimicvideo.VideoSampleActivity;
import com.example.john.mimicvideo.model.Like;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.example.john.mimicvideo.view.AutoPlayVideo.AAH_CustomViewHolder;
import com.example.john.mimicvideo.view.AutoPlayVideo.AAH_VideosAdapter;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/4/17.
 */

public class SameVideoContentAutoPlayAdapter extends AAH_VideosAdapter {

    Context context;
    List<VideoContent> sameVideoContentList;
    private JSONParser jsonParser = new JSONParser();
    private SharePreferenceDB sharePreferenceDB;
    private int user_id;

    private int VIDEO_SAMPLE = 0;
    private int VIDEO_CONTENT = 1;

    public SameVideoContentAutoPlayAdapter(Context context, List<VideoContent>sameVideoContentList) {
        this.context = context;
        this.sameVideoContentList = sameVideoContentList;
        sharePreferenceDB = new SharePreferenceDB(context);
        user_id = sharePreferenceDB.getInt("id");
    }

    @Override
    public AAH_CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIDEO_SAMPLE){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pager_row_video_sample, parent, false);
            return new VideoSampleViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_same_video_content, parent, false);
            return new VideoContentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final AAH_CustomViewHolder holder, final int position) {
        if(holder instanceof VideoSampleViewHolder){
            VideoSampleViewHolder videoSampleViewHolder = (VideoSampleViewHolder)holder;

            final VideoContent videoContent = sameVideoContentList.get(position);

            videoSampleViewHolder.openVideoSampleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, VideoSampleActivity.class);
                    ((Activity)context).startActivity(intent);
                }
            });

            videoSampleViewHolder.reportImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    new ReportSubmit("").execute();
                }
            });

            videoSampleViewHolder.shareTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, ShareActivity.class);
                    intent.putExtra("videoContentUrl", videoContent.url);
                    context.startActivity(intent);
                }
            });

            videoSampleViewHolder.videoContentTitleTxt.setText(videoContent.title);
        }else{
            final VideoContentViewHolder videoContentViewHolder = (VideoContentViewHolder)holder;
            final VideoContent videoContent = sameVideoContentList.get(position);
//        showVideo(position);
            videoContentViewHolder.ownerNameTxt.setText(videoContent.owner.name);
            videoContentViewHolder.videoContentTitleTxt.setText(videoContent.title);

            videoContentViewHolder.commentAmountTxt.setText(String.valueOf(videoContent.commentAmount));
            videoContentViewHolder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));

            Glide.with(context)
                    .load(videoContent.owner.profile)
                    .into(videoContentViewHolder.ownerProfileImg);

            Glide.with(context)
                    .load(videoContent.url)
                    .into(holder.getAAH_ImageView());

            holder.setLooping(true);

            holder.setVideoUrl(videoContent.url);

            videoContentViewHolder.openCommentImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("video_content_id", videoContent.id);
                    intent.setClass(context, CommentActivity.class);
                    context.startActivity(intent);
                }
            });


            videoContentViewHolder.openVideoSampleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, FantasticCameraActivity.class);
                    intent.putExtra("videoContentUrl", videoContent.url);
                    context.startActivity(intent);
                }
            });

            for(int i = 0; i < videoContent.likeList.size(); i++){
                if(videoContent.likeList.get(i).user_id == user_id){
                    if(videoContent.likeList.get(i).is_click == 1){
                        videoContentViewHolder.giveLikeImg.setImageResource(R.drawable.smile_like_yellow);
                    }else{
                        videoContentViewHolder.giveLikeImg.setImageResource(R.drawable.smile_like);
                    }
                }else{
                    videoContentViewHolder.giveLikeImg.setImageResource(R.drawable.smile_like);
                }
            }
            videoContentViewHolder.giveLikeImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(sharePreferenceDB.getInt("id") != 0){
                        if(view.getBackground() == context.getResources().getDrawable(R.drawable.smile_like_yellow)){
                            view.setBackgroundResource(R.drawable.smile_like);
                            new UpdateLikeAmount(user_id, videoContent.id, 0).execute();
                            sameVideoContentList.get(position).likeAmount =  sameVideoContentList.get(position).likeAmount - 1;
                            videoContentViewHolder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));
                            Like like = new Like();
                            like.user_id = sharePreferenceDB.getInt("id");
                            like.video_content_id = sameVideoContentList.get(position).id;
                            sameVideoContentList.get(position).likeList.remove(like);
                            ArrayList<String>clickFavoriteIdArrayList = sharePreferenceDB.getListString("clickFavoriteIdArrayList");
                            clickFavoriteIdArrayList.remove(String.valueOf(sameVideoContentList.get(position).id));
                            sharePreferenceDB.putListString("clickFavoriteIdArrayList", clickFavoriteIdArrayList);
                        }else{
                            view.setBackgroundResource(R.drawable.smile_like_yellow);
                            new UpdateLikeAmount(user_id, videoContent.id, 1).execute();
                            sameVideoContentList.get(position).likeAmount = sameVideoContentList.get(position).likeAmount + 1;
                            videoContentViewHolder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));
                            Like like = new Like();
                            like.user_id = sharePreferenceDB.getInt("id");
                            like.video_content_id = sameVideoContentList.get(position).id;
                            sameVideoContentList.get(position).likeList.add(like);
                            ArrayList<String>clickFavoriteIdArrayList = sharePreferenceDB.getListString("clickFavoriteIdArrayList");
                            clickFavoriteIdArrayList.add(String.valueOf(sameVideoContentList.get(position).id));
                            sharePreferenceDB.putListString("clickFavoriteIdArrayList", clickFavoriteIdArrayList);
                        }
                    }else{
                        Intent intent = new Intent();
                        intent.setClass(context, LoginActivity.class);
                        context.startActivity(intent);
                    }
                }
            });

            videoContentViewHolder.ownerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, OtherProfileActivity.class);
                    intent.putExtra("id", sameVideoContentList.get(position).owner.id);
                    intent.putExtra("name", sameVideoContentList.get(position).owner.name);
                    intent.putExtra("profile", sameVideoContentList.get(position).owner.profile);
                    context.startActivity(intent);
                }
            });

            videoContentViewHolder.shareTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, ShareActivity.class);
                    context.startActivity(intent);
                }
            });

            videoContentViewHolder.reportTxt.setTypeface(ApplicationService.getFont());
            videoContentViewHolder.reportTxt.setText(R.string.fa_flag);
            videoContentViewHolder.reportTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            final GestureDetector gestureDetector = new
                    GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            if (holder.isPlaying()) {
                                holder.pauseVideo();
                                holder.setPaused(true);
                            } else {
                                holder.playVideo();
                                holder.setPaused(false);
                            }
                            return true;
                        }
                    }
            );

            holder.getAAH_CustomVideoView().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, @NonNull MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
//        if(position == 0){
//           return VIDEO_SAMPLE;
//        }else{
//            return VIDEO_CONTENT;
//        }
        return VIDEO_CONTENT;
    }

    @Override
    public int getItemCount() {
        return sameVideoContentList.size();
    }

    public void setSameVideoContentList(List<VideoContent> sameVideoContentList){
        this.sameVideoContentList = sameVideoContentList;
        notifyDataSetChanged();
    }

    public class VideoSampleViewHolder extends AAH_CustomViewHolder{
        View itemView;
        public VideoView videoContentVideoView;
        public ImageView openVideoSampleImg;
        public ImageView reportImg;
        public TextView shareTxt;
        public TextView videoContentTitleTxt;

        public VideoSampleViewHolder(View itemView){
            super(itemView);
            this.itemView = itemView;
            videoContentVideoView = itemView.findViewById(R.id.videoContentVideoView);
            openVideoSampleImg = itemView.findViewById(R.id.openVideoSampleImg);
            reportImg = itemView.findViewById(R.id.reportImg);
            shareTxt = itemView.findViewById(R.id.shareTxt);
            videoContentTitleTxt = itemView.findViewById(R.id.videoContentTitleTxt);
        }
    }

    public class VideoContentViewHolder extends AAH_CustomViewHolder {
        View itemView;
        public SimpleExoPlayerView videoContentPlayerView;
        private ImageView ownerProfileImg;
        public TextView ownerNameTxt;
        public ImageView openCommentImg;
        public ImageView openVideoSampleImg;
        public ImageView giveLikeImg;
        public TextView shareTxt;
        public TextView videoContentTitleTxt;
        public LinearLayout ownerLayout;
        public TextView commentAmountTxt;
        public TextView likeAmountTxt;
        private TextView reportTxt;

        public VideoContentViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            videoContentPlayerView = itemView.findViewById(R.id.videoContentPlayerView);
            ownerProfileImg = itemView.findViewById(R.id.ownerProfileImg);
            ownerNameTxt = itemView.findViewById(R.id.ownerNameTxt);
            openCommentImg = itemView.findViewById(R.id.openCommentImg);
            openVideoSampleImg = itemView.findViewById(R.id.openVideoSampleImg);
            giveLikeImg = itemView.findViewById(R.id.giveLikeImg);
            videoContentTitleTxt = itemView.findViewById(R.id.videoContentTitleTxt);
            ownerLayout = itemView.findViewById(R.id.ownerLayout);
            shareTxt = itemView.findViewById(R.id.shareTxt);
            shareTxt.setTypeface(ApplicationService.getFont());
            shareTxt.setText(R.string.fa_smile_o);
            commentAmountTxt = itemView.findViewById(R.id.commentAmountTxt);
            likeAmountTxt = itemView.findViewById(R.id.likeAmountTxt);
            reportTxt = itemView.findViewById(R.id.reportTxt);
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
