package com.example.john.mimicvideo.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.CameraActivity;
import com.example.john.mimicvideo.CommentActivity;
import com.example.john.mimicvideo.LoginActivity;
import com.example.john.mimicvideo.OtherProfileActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.SameVideoContentActivity;
import com.example.john.mimicvideo.ShareActivity;
import com.example.john.mimicvideo.VideoSampleActivity;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.GridLayoutManagerWithSmoothScroller;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/3/25.
 */

public class MainVideoContentPagerAdapter extends PagerAdapter {
    Context context;
    List<VideoContent>mainVideoContentList;
    private SimpleExoPlayerView videoContentPlayerView;
    private ImageView ownerProfileImg;
    private TextView ownerNameTxt;
    private ImageView openCommentImg;
    private ImageView openVideoSampleImg;
    private ImageView openSameVideoContentImg;
    private ImageView giveLikeImg;
    private TextView shareTxt;
    private TextView videoContentTitleTxt;
    private LinearLayout ownerLayout;
    private TextView commentAmountTxt;
    private TextView likeAmountTxt;
    private JSONParser jsonParser = new JSONParser();
    private SharePreferenceDB sharePreferenceDB;
    private int user_id;

    public MainVideoContentPagerAdapter(Context context, List<VideoContent> mainVideoContentList){
        this.context = context;
        this.mainVideoContentList = mainVideoContentList;
    }

    @Override
    public int getCount() {
        return mainVideoContentList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.pager_row_video_content, null);
        container.addView(linearLayout);
        videoContentPlayerView = linearLayout.findViewById(R.id.videoContentPlayerView);
        ownerProfileImg = linearLayout.findViewById(R.id.ownerProfileImg);
        ownerNameTxt = linearLayout.findViewById(R.id.ownerNameTxt);
        openCommentImg = linearLayout.findViewById(R.id.openCommentImg);
        openVideoSampleImg = linearLayout.findViewById(R.id.openVideoSampleImg);
        openSameVideoContentImg = linearLayout.findViewById(R.id.openSameVideoContentImg);
        giveLikeImg = linearLayout.findViewById(R.id.giveLikeImg);
        videoContentTitleTxt = linearLayout.findViewById(R.id.videoContentTitleTxt);
        ownerLayout = linearLayout.findViewById(R.id.ownerLayout);
        shareTxt = linearLayout.findViewById(R.id.shareTxt);
        shareTxt.setTypeface(ApplicationService.getFont());
        shareTxt.setText(R.string.fa_share_square_o);
        commentAmountTxt = linearLayout.findViewById(R.id.commentAmountTxt);
        likeAmountTxt = linearLayout.findViewById(R.id.likeAmountTxt);


        final VideoContent videoContent = mainVideoContentList.get(position);
        sharePreferenceDB = new SharePreferenceDB(context);
        user_id = sharePreferenceDB.getInt("id");
//        showVideo(position);
        ownerNameTxt.setText(videoContent.owner.name);
        videoContentTitleTxt.setText(videoContent.title);

        commentAmountTxt.setText(String.valueOf(videoContent.commentAmount));
        likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));

        Glide.with(context)
                .load(videoContent.owner.profile)
                .into(ownerProfileImg);

        openCommentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("video_content_id", videoContent.id);
                intent.setClass(context, CommentActivity.class);
                context.startActivity(intent);
            }
        });

        openVideoSampleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, CameraActivity.class);
                intent.putExtra("video_sample_id", videoContent.videoSampleId);
                intent.putExtra("video_sample_url", videoContent.videoSampleUrl);
                context.startActivity(intent);
            }
        });

        openSameVideoContentImg.setOnClickListener(new View.OnClickListener() {
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
                    giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.com_facebook_button_background_color));
                }else{
                    giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.white));
                }
            }else{
                giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }
        giveLikeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharePreferenceDB.getInt("id") != 0){
                    if(((ColorDrawable)view.getBackground()).getColor() == context.getResources().getColor(R.color.com_facebook_button_background_color)){
                        view.setBackgroundColor(context.getResources().getColor(R.color.white));
                        new UpdateLikeAmount(user_id, videoContent.id, 0).execute();
                    }else{
                        view.setBackgroundColor(context.getResources().getColor(R.color.com_facebook_button_background_color));
                        new UpdateLikeAmount(user_id, videoContent.id, 1).execute();
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setClass(context, LoginActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        ownerLayout.setOnClickListener(new View.OnClickListener() {
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

        shareTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, ShareActivity.class);
                context.startActivity(intent);
            }
        });

//        shareImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Intent intent = new Intent();
////                intent.setClass(context, ShareActivity.class);
////                context.startActivity(intent);
//
//                final Dialog dialog = new Dialog(context, R.style.selectorDialog);
//                dialog.setContentView(R.layout.dialog_report);
//                RecyclerView reportDescriptionRV = dialog.findViewById(R.id.reportDescriptionRV);
//                Button reportSubmitBtn = dialog.findViewById(R.id.reportSubmitBtn);
//
//                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
//                ReportDescriptionAdapter reportDescriptionAdapter = new ReportDescriptionAdapter(context);
//                reportDescriptionRV.setLayoutManager(layoutManager);
//                reportDescriptionRV.setAdapter(reportDescriptionAdapter);
//
//                reportSubmitBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        new ReportSubmit("").execute();
//                    }
//                });
//
//
//                // 由程式設定 Dialog 視窗外的明暗程度, 亮度從 0f 到 1f
//                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//                lp.dimAmount = 0.5f;
//                dialog.getWindow().setAttributes(lp);
//                dialog.show();
//            }
//        });



        return linearLayout;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(videoContentPlayerView.getPlayer() != null){
            videoContentPlayerView.getPlayer().release();
        }
        container.removeView((View) object);
    }

    public void showVideo(int position){
        SimpleExoPlayer player ;
        // 1.創建一个默認TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        // 2.創建一个默認的LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3.創建播放器
        player = ExoPlayerFactory.newSimpleInstance(context,trackSelector,loadControl);
        // 将player关联到View上
        videoContentPlayerView.setPlayer(player);
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "yourApplicationName"), bandwidthMeter2);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource;
        // test hls
        //url="http://hls.videocc.net/ce0812b122/c/ce0812b122c492470605bc47d3388a09_3.m3u8";
        if(mainVideoContentList.get(position).url.contains(".m3u8")){
//            videoSource =new HlsMediaSource(Uri.parse(url),dataSourceFactory,null,null);
            videoSource = new ExtractorMediaSource(Uri.parse(mainVideoContentList.get(position).url),
                    dataSourceFactory, extractorsFactory, null, null);
        }else{
            //test mp4
            videoSource = new ExtractorMediaSource(Uri.parse(mainVideoContentList.get(position).url),
                    dataSourceFactory, extractorsFactory, null, null);
        }

        player.prepare(videoSource);
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
