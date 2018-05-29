package com.example.john.mimicvideo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.adapter.OtherUserVideoContentAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.Like;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationParameter;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.GridLayoutManagerWithSmoothScroller;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SearchItemDecoration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OtherProfileActivity extends BaseActivity {
    ImageView profileImg;
    TextView nameTxt;
    TextView backTxt;
    Button subscribeBtn;
    RecyclerView userVideoContentRV;
    JSONParser jsonParser = new JSONParser();

    private List<VideoContent>userVideoContentList = new ArrayList<>();
    private OtherUserVideoContentAdapter userVideoContentAdapter;
    private int video_content_amount = 20;
    private int current_size = 100;
    private boolean is_loading = false;

    int id;
    String name;
    String profile;

    //for subscribe btn
    int flag = 0;
    ArrayList<String>userIdIFollowArrayList = new ArrayList<>();

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(ApplicationParameter.BACK_SHOW_VIDEO_CONTENT, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        profileImg = findViewById(R.id.profileImg);
        nameTxt = findViewById(R.id.nameTxt);
        backTxt = findViewById(R.id.backTxt);
        subscribeBtn = findViewById(R.id.subscribebtn);
        userVideoContentRV = findViewById(R.id.userVideoContentRV);

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(getString(R.string.fa_angle_left));

        id = getIntent().getIntExtra("id",0);
        name = getIntent().getStringExtra("name");
        profile = getIntent().getStringExtra("profile");

        if(id == SharePreferenceDB.getInstance(OtherProfileActivity.this).getInt("id")){
            subscribeBtn.setVisibility(View.GONE);
        }

        userIdIFollowArrayList = SharePreferenceDB.getInstance(OtherProfileActivity.this).getListString("userIdIFollowArrayList");
        for(int i =0; i < userIdIFollowArrayList.size(); i++){
            if(String.valueOf(id).equals(userIdIFollowArrayList.get(i))){
                subscribeBtn.setActivated(true);
                subscribeBtn.setText("已訂閱");
                flag = 1;
            }
        }

        GridLayoutManagerWithSmoothScroller layoutManager = new GridLayoutManagerWithSmoothScroller(OtherProfileActivity.this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        userVideoContentAdapter = new OtherUserVideoContentAdapter(OtherProfileActivity.this, userVideoContentList);
        userVideoContentRV.setLayoutManager(layoutManager);
        userVideoContentRV.addItemDecoration(new SearchItemDecoration(OtherProfileActivity.this, 0));
        userVideoContentRV.setAdapter(userVideoContentAdapter);

        subscribeBtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch(flag) {
                    case 0:
                        subscribeBtn.setActivated(true);
                        subscribeBtn.setText("已訂閱");
                        subscribeBtn.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.heart_subscribed), null, null);
                        flag = 1;
                        new CreateSubscribe(id, SharePreferenceDB.getInstance(OtherProfileActivity.this).getInt("id")).execute();
                        userIdIFollowArrayList.add(String.valueOf(id));
                        SharePreferenceDB.getInstance(OtherProfileActivity.this).putListString("userIdIFollowArrayList", userIdIFollowArrayList);

                        break;
                    case 1:
                        subscribeBtn.setActivated(false);
                        subscribeBtn.setText("訂閱");
                        subscribeBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.heart_unsubscribe),null, null, null);
                        flag = 0;
                        new DeleteSubscribe(id, SharePreferenceDB.getInstance(OtherProfileActivity.this).getInt("id")).execute();
                        userIdIFollowArrayList.remove(String.valueOf(id));
                        SharePreferenceDB.getInstance(OtherProfileActivity.this).putListString("userIdIFollowArrayList", userIdIFollowArrayList);
                        break;
                }
            }
        });

        userVideoContentRV.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                super.onScrollStateChanged(recyclerView, scrollState);

                if(((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() > (current_size - 50) && !is_loading){

                    is_loading = true;
                    video_content_amount = video_content_amount + 100;
                    new GetUserVideoContent(video_content_amount, id).execute();
                }
            }
        });

        nameTxt.setText(name);

        Glide.with(this)
                .load(profile)
                .into(profileImg);

        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(ApplicationParameter.BACK_SHOW_VIDEO_CONTENT, intent);
                finish();
            }
        });

        new GetUserVideoContent(video_content_amount, id).execute();
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


            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            userVideoContentAdapter.setVideoContentList(userVideoContentList);
        }

    }


    class CreateSubscribe extends AsyncTask<String, String, JSONObject> {
        int owner_id;
        int follower_id;


        CreateSubscribe(int owner_id, int follower_id){
            this.owner_id = owner_id;
            this.follower_id = follower_id;
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
        protected JSONObject doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("owner_id", String.valueOf(owner_id)));
            params.add(new BasicNameValuePair("follower_id", String.valueOf(follower_id)));


            // getting JSON Object
            // Note that create product url accepts POST method
            //php名待改
            JSONObject jsonObject = jsonParser.makeHttpRequest(Api.baseUrl + "create_subscribe.php",
                    "POST", params);

            if(jsonObject != null){
                // check log cat fro response
                Log.d("Create Response", jsonObject.toString());
                return jsonObject;
            }else{
                return null;
            }
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject != null){
                if(jsonObject.optInt("success") == 1){

                }else{

                }
            }else {

            }
        }

    }

    class DeleteSubscribe extends AsyncTask<String, String, JSONObject> {
        int owner_id;
        int follower_id;


        DeleteSubscribe(int owner_id, int follower_id){
            this.owner_id = owner_id;
            this.follower_id = follower_id;
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
        protected JSONObject doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("owner_id", String.valueOf(owner_id)));
            params.add(new BasicNameValuePair("follower_id", String.valueOf(follower_id)));


            // getting JSON Object
            // Note that create product url accepts POST method
            //php名待改
            JSONObject jsonObject = jsonParser.makeHttpRequest(Api.baseUrl + "delete_subscribe.php",
                    "POST", params);

            if(jsonObject != null){
                // check log cat fro response
                Log.d("Create Response", jsonObject.toString());
                return jsonObject;
            }else{
                return null;
            }
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject != null){
                if(jsonObject.optInt("success") == 1){

                }else{

                }
            }else {

            }
        }

    }
}
