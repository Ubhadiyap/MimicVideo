package com.example.john.mimicvideo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.adapter.UserVideoContentAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.Like;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.GridLayoutManagerWithSmoothScroller;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SearchItemDecoration;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.facebook.login.LoginManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {
    ImageView profileImg;
    TextView nameTxt;
    TextView backTxt;
    ImageView settingImg;
    RecyclerView userVideoContentRV;
    TextView myVideoContentTabTxt;
    TextView myVideoLikeTabTxt;

    JSONParser jsonParser = new JSONParser();
    //private SharePreferenceDB sharePreferenceDB;
    private List<VideoContent>userVideoContentList = new ArrayList<>();
    private List<VideoContent>subscribeVideoContentList = new ArrayList<>();
    private UserVideoContentAdapter userVideoContentAdapter;
    private int video_content_amount = 100;
    private int like_video_content_amount = 100;
    private int current_size = 100;
    private boolean is_loading = false;

    // below for fb
    private LoginManager loginManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImg = findViewById(R.id.profileImg);
        nameTxt = findViewById(R.id.nameTxt);
        backTxt = findViewById(R.id.backTxt);
        userVideoContentRV = findViewById(R.id.userVideoContentRV);
        myVideoContentTabTxt = findViewById(R.id.myVideoContentTabTxt);
        myVideoLikeTabTxt = findViewById(R.id.myLikeTabTxt);
        settingImg = findViewById(R.id.settingImg);
        //final SharePreferenceDB sharePreferenceDB = new SharePreferenceDB(this);
        loginManager = LoginManager.getInstance();


        nameTxt.setText(SharePreferenceDB.getInstance(ProfileActivity.this).getString("name"));

        Glide.with(this)
                .load(SharePreferenceDB.getInstance(ProfileActivity.this).getString("profile"))
                .into(profileImg);

        final GridLayoutManagerWithSmoothScroller layoutManager = new GridLayoutManagerWithSmoothScroller(ProfileActivity.this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        userVideoContentAdapter = new UserVideoContentAdapter(ProfileActivity.this, userVideoContentList);
        userVideoContentRV.setLayoutManager(layoutManager);
        userVideoContentRV.addItemDecoration(new SearchItemDecoration(ProfileActivity.this, 0));
        userVideoContentRV.setAdapter(userVideoContentAdapter);

        userVideoContentRV.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                super.onScrollStateChanged(recyclerView, scrollState);

                if(((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() > (current_size - 50) && !is_loading){

                    is_loading = true;
                    video_content_amount = video_content_amount + 100;
                    new GetUserVideoContent(video_content_amount, SharePreferenceDB.getInstance(ProfileActivity.this).getInt("id")).execute();
                }
            }
        });

        myVideoContentTabTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myVideoContentTabTxt.setBackgroundColor(Color.parseColor("#888888"));
                myVideoLikeTabTxt.setBackgroundColor(Color.parseColor("#DDDDDD"));
                userVideoContentAdapter.setVideoContentList(userVideoContentList, 0);
            }
        });
        myVideoLikeTabTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myVideoLikeTabTxt.setBackgroundColor(Color.parseColor("#888888"));
                myVideoContentTabTxt.setBackgroundColor(Color.parseColor("#DDDDDD"));
                userVideoContentAdapter.setVideoContentList(subscribeVideoContentList, 1);
            }
        });

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(getString(R.string.fa_angle_left));

        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        settingImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ProfileActivity.this, R.style.selectorDialog);
                dialog.setContentView(R.layout.dialog_setting);
                TextView backTxt = dialog.findViewById(R.id.backTxt);
                TextView logoutTxt = dialog.findViewById(R.id.logoutTxt);

                backTxt.setTypeface(ApplicationService.getFont());
                backTxt.setText(getString(R.string.fa_times));
                backTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                logoutTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        SharePreferenceDB.getInstance(ProfileActivity.this).clear();
                        loginManager.logOut();
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);;
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

                // 由程式設定 Dialog 視窗外的明暗程度, 亮度從 0f 到 1f
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount = 0.5f;
                dialog.getWindow().setAttributes(lp);
                dialog.show();

            }
        });


        new GetUserVideoContent(video_content_amount, SharePreferenceDB.getInstance(ProfileActivity.this).getInt("id")).execute();
        new GetUserSubscribeVideoContent(like_video_content_amount, SharePreferenceDB.getInstance(ProfileActivity.this).getInt("id")).execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new GetUserVideoContent(video_content_amount, SharePreferenceDB.getInstance(ProfileActivity.this).getInt("id")).execute();
    }

    class GetUserVideoContent extends AsyncTask<String, String, String> {
        int video_content_amount;
        int user_id;

        GetUserVideoContent(int video_content_amount, int user_id){
            this.video_content_amount = video_content_amount;
            this.user_id = user_id;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
//            String name = inputName.getText().toString();
//            String price = inputPrice.getText().toString();
//            String description = inputDesc.getText().toString();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("video_content_amount", String.valueOf(video_content_amount)));
            params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray videoContentJSONArray = jsonParser.makeHttpRequestArray("http://1.34.63.239/get_user_video_content.php",
                    "POST", params);

            if(videoContentJSONArray != null){
                Log.d("Create Response", videoContentJSONArray.toString());

                try{
                    userVideoContentList.clear();

                    for(int i = 0; i < videoContentJSONArray.length(); i++){
                        VideoContent videoContent = new VideoContent();
                        videoContent.id = videoContentJSONArray.optJSONObject(i).optInt("id");
                        videoContent.videoSampleId = videoContentJSONArray.optJSONObject(i).optInt("video_sample_id");
                        User user = new User();
                        JSONObject userJsonObject = videoContentJSONArray.optJSONObject(i).optJSONObject("user");
                        user.id = userJsonObject.optInt("id");
                        user.name = userJsonObject.optString("name");
                        user.profile = userJsonObject.optString("profile");
                        videoContent.owner = user;
                        JSONArray likeJsonArray = videoContentJSONArray.optJSONObject(i).optJSONArray("like");
                        List<Like>likeList = new ArrayList<>();
                        for(int j = 0; j < likeJsonArray.length(); j++){
                            Like like = new Like();
                            like.user_id =  likeJsonArray.optJSONObject(j).optInt("user_id");
                            like.is_click = likeJsonArray.optJSONObject(j).optInt("is_click");
                            likeList.add(like);
                        }
                        videoContent.likeList = likeList;
                        videoContent.likeAmount = likeList.size();
                        JSONArray commentJsonArray = videoContentJSONArray.optJSONObject(i).optJSONArray("comment");
                        videoContent.commentAmount = commentJsonArray.length();
                        videoContent.title = videoContentJSONArray.optJSONObject(i).optString("title");
                        videoContent.url =  Api.baseUrl + "video_content/" + videoContentJSONArray.optJSONObject(i).optString("path");
                        userVideoContentList.add(videoContent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }


            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            userVideoContentAdapter.setVideoContentList(userVideoContentList, 0);
        }

    }

    class GetUserSubscribeVideoContent extends AsyncTask<String, String, String> {
        int video_content_amount;
        int user_id;

        GetUserSubscribeVideoContent(int video_content_amount, int user_id){
            this.video_content_amount = video_content_amount;
            this.user_id = user_id;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
//            String name = inputName.getText().toString();
//            String price = inputPrice.getText().toString();
//            String description = inputDesc.getText().toString();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("video_content_amount", String.valueOf(video_content_amount)));
            params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray videoContentJSONArray = jsonParser.makeHttpRequestArray("http://1.34.63.239/get_user_subscribe_video_content.php",
                    "POST", params);

            if(videoContentJSONArray != null){
                Log.d("Create Response", videoContentJSONArray.toString());

                try{
                    subscribeVideoContentList.clear();

                    for(int i = 0; i < videoContentJSONArray.length(); i++){
                        VideoContent videoContent = new VideoContent();
                        videoContent.id = videoContentJSONArray.optJSONObject(i).optInt("id");
                        videoContent.videoSampleId = videoContentJSONArray.optJSONObject(i).optInt("video_sample_id");
                        User user = new User();
                        JSONObject userJsonObject = videoContentJSONArray.optJSONObject(i).optJSONObject("user");
                        user.id = userJsonObject.optInt("id");
                        user.name = userJsonObject.optString("name");
                        user.profile = userJsonObject.optString("profile");
                        videoContent.owner = user;
                        JSONArray likeJsonArray = videoContentJSONArray.optJSONObject(i).optJSONArray("like");
                        List<Like>likeList = new ArrayList<>();
                        for(int j = 0; j < likeJsonArray.length(); j++){
                            Like like = new Like();
                            like.user_id =  likeJsonArray.optJSONObject(j).optInt("user_id");
                            like.is_click = likeJsonArray.optJSONObject(j).optInt("is_click");
                            likeList.add(like);
                        }
                        videoContent.likeList = likeList;
                        videoContent.likeAmount = likeList.size();
                        JSONArray commentJsonArray = videoContentJSONArray.optJSONObject(i).optJSONArray("comment");
                        videoContent.commentAmount = commentJsonArray.length();
                        videoContent.title = videoContentJSONArray.optJSONObject(i).optString("title");
                        videoContent.url =  Api.baseUrl + "video_content/" + videoContentJSONArray.optJSONObject(i).optString("path");
                        subscribeVideoContentList.add(videoContent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }


            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            //userVideoContentAdapter.setVideoContentList(subscribeVideoContentList);
        }

    }
}
