package com.example.john.mimicvideo.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.CameraActivity;
import com.example.john.mimicvideo.CommentActivity;
import com.example.john.mimicvideo.LoadingActivity;
import com.example.john.mimicvideo.LoginActivity;
import com.example.john.mimicvideo.OtherProfileActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.SameVideoContentActivity;
import com.example.john.mimicvideo.ShareActivity;
import com.example.john.mimicvideo.TestVideoActivity;
import com.example.john.mimicvideo.model.Like;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.example.john.mimicvideo.view.AutoPlayVideo.AAH_CustomViewHolder;
import com.example.john.mimicvideo.view.AutoPlayVideo.AAH_VideosAdapter;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import junit.framework.Test;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/4/16.
 */

public class MainVideoContentAutoPlayAdapter extends AAH_VideosAdapter {
    Context context;
    List<VideoContent> mainVideoContentList;
    private JSONParser jsonParser = new JSONParser();
    private SharePreferenceDB sharePreferenceDB;
    private int user_id;
    private ArrayList<String>clickFavoriteIdArrayList = new ArrayList<>();

    public MainVideoContentAutoPlayAdapter(Context context, List<VideoContent>mainVideoContentList) {
        this.context = context;
        this.mainVideoContentList = mainVideoContentList;
        sharePreferenceDB = new SharePreferenceDB(context);
        user_id = sharePreferenceDB.getInt("id");
        this.clickFavoriteIdArrayList = sharePreferenceDB.getListString("clickFavoriteIdArrayList");
    }

    @Override
    public AAH_CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pager_row_video_content, parent, false);
        return new MainVideoContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AAH_CustomViewHolder aah_customViewHolder, final int position) {
        final MainVideoContentViewHolder holder = (MainVideoContentViewHolder)aah_customViewHolder;
        final VideoContent videoContent = mainVideoContentList.get(position);

//        showVideo(position);
        holder.ownerNameTxt.setText(videoContent.owner.name);
        holder.videoContentTitleTxt.setText(videoContent.title);

        holder.commentAmountTxt.setText(String.valueOf(videoContent.commentAmount));
        holder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));

        Glide.with(context)
                .load(videoContent.owner.profile)
                .into(holder.ownerProfileImg);

        Glide.with(context)
                .load(videoContent.url)
                .into(holder.getAAH_ImageView());

        holder.setLooping(true);

        holder.setVideoUrl(videoContent.url);

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
                intent.setClass(context, LoadingActivity.class);
                intent.putExtra("videoContentUrl", videoContent.url);
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
                    holder.giveLikeImg.setImageResource(R.drawable.smile_like_yellow);
                }else{
                    holder.giveLikeImg.setImageResource(R.drawable.smile_like);
                }
            }else{
                holder.giveLikeImg.setImageResource(R.drawable.smile_like);
            }
        }
        holder.giveLikeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharePreferenceDB.getInt("id") != 0){
                    if(view.getBackground() == context.getResources().getDrawable(R.drawable.smile_like_yellow)){
                        view.setBackgroundResource(R.drawable.smile_like);
                        new UpdateLikeAmount(user_id, videoContent.id, 0).execute();
                        mainVideoContentList.get(position).likeAmount =  mainVideoContentList.get(position).likeAmount - 1;
                        holder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));
                        Like like = new Like();
                        like.user_id = sharePreferenceDB.getInt("id");
                        like.video_content_id = mainVideoContentList.get(position).id;
                        mainVideoContentList.get(position).likeList.remove(like);

                    }else{
                        view.setBackgroundResource(R.drawable.smile_like_yellow);
                        new UpdateLikeAmount(user_id, videoContent.id, 1).execute();
                        mainVideoContentList.get(position).likeAmount = mainVideoContentList.get(position).likeAmount + 1;
                        holder.likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));
                        Like like = new Like();
                        like.user_id = sharePreferenceDB.getInt("id");
                        like.video_content_id = mainVideoContentList.get(position).id;
                        mainVideoContentList.get(position).likeList.add(like);
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
                intent.putExtra("videoContentUrl", mainVideoContentList.get(position).url);
                context.startActivity(intent);
            }
        });


        holder.reportTxt.setTypeface(ApplicationService.getFont());
        holder.reportTxt.setText(R.string.fa_flag);
        holder.reportTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context, R.style.selectorDialog);
                dialog.setContentView(R.layout.dialog_report);
                TextView cancelTxt = dialog.findViewById(R.id.cancelTxt);
                RecyclerView reportDescriptionRV = dialog.findViewById(R.id.reportDescriptionRV);
                Button reportSubmitBtn = dialog.findViewById(R.id.reportSubmitBtn);

                cancelTxt.setTypeface(ApplicationService.getFont());
                cancelTxt.setText(R.string.fa_times);

                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                ReportDescriptionAdapter reportDescriptionAdapter = new ReportDescriptionAdapter(context);
                reportDescriptionRV.setLayoutManager(layoutManager);
                reportDescriptionRV.setAdapter(reportDescriptionAdapter);

                cancelTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                reportSubmitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new ReportSubmit("").execute();
                    }
                });


                // 由程式設定 Dialog 視窗外的明暗程度, 亮度從 0f 到 1f
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount = 0.5f;
                dialog.getWindow().setAttributes(lp);
                dialog.show();
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

    @Override
    public int getItemCount() {
        return mainVideoContentList.size();
    }

    public void setMainVideoContentList(List<VideoContent> mainVideoContentList){
        this.mainVideoContentList = mainVideoContentList;
        notifyDataSetChanged();
    }

    public class MainVideoContentViewHolder extends AAH_CustomViewHolder {
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
        public ImageView videoPlayIconImg;
        public TextView  reportTxt;

        public MainVideoContentViewHolder(View itemView) {
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
            videoPlayIconImg = itemView.findViewById(R.id.videoPlayIconImg);
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

    //send the report description
    class ReportSubmit extends AsyncTask<String, String, String> {
        String reportDescription;

        ReportSubmit(String reportDescription) {
            this.reportDescription = reportDescription;
        }

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {
//            String name = inputName.getText().toString();
//            String price = inputPrice.getText().toString();
//            String description = inputDesc.getText().toString();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("type", "0"));
            params.add(new BasicNameValuePair("reportDescription", reportDescription));


            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonObject = jsonParser.makeHttpRequest("http://1.34.63.239/create_report.php",
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", jsonObject.toString());

//            // check for success tag
//            try {
//                int success = json.getInt(TAG_SUCCESS);
//
//                if (success == 1) {
//                    // successfully created product
//                    Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
//                    startActivity(i);
//
//                    // closing this screen
//                    finish();
//                } else {
//                    // failed to create product
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        }

    }

}
