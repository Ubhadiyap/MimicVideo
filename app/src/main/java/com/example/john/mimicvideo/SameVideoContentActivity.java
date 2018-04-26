package com.example.john.mimicvideo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.example.john.mimicvideo.adapter.MainVideoContentAutoPlayAdapter;
import com.example.john.mimicvideo.adapter.SameVideoContentAutoPlayAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.fragment.MainFragment;
import com.example.john.mimicvideo.model.Like;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.example.john.mimicvideo.view.AutoPlayVideo.AAH_CustomRecyclerView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SameVideoContentActivity extends BaseActivity {
    JSONParser jsonParser = new JSONParser();
    List<VideoContent>sameVideoContentList = new ArrayList<>();
    SameVideoContentAutoPlayAdapter sameVideoContentAutoPlayAdapter;
    ViewPager sameVideoContentVP;
    RecyclerView sameVideoContentRV;
    AAH_CustomRecyclerView sameVideoContentAutoPlayRV;
    private int video_sample_id;
    private int video_content_amount = 20;
    private int current_size = 100;
    private TextView backTxt;
    private boolean is_loading = false;
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_same_video_content);
        sameVideoContentVP = findViewById(R.id.sameVideoContentVP);
        backTxt = findViewById(R.id.backTxt);
        sameVideoContentRV = findViewById(R.id.sameVideoContentRV);
        sameVideoContentAutoPlayRV = findViewById(R.id.sameVideoContentAutoPlayRV);


        LinearLayoutManager layoutManager = new LinearLayoutManager(SameVideoContentActivity.this, LinearLayoutManager.VERTICAL, false);
        sameVideoContentAutoPlayRV.setLayoutManager(layoutManager);
        sameVideoContentAutoPlayAdapter = new SameVideoContentAutoPlayAdapter(this, sameVideoContentList);
        sameVideoContentAutoPlayRV.setAdapter(sameVideoContentAutoPlayAdapter);
        prepareAutoPlayRV();
        sameVideoContentAutoPlayRV.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                super.onScrollStateChanged(recyclerView, scrollState);

                if(((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() > (current_size - 50) && !is_loading){
                    is_loading = true;
                    video_content_amount = video_content_amount + 100;
                    new GetMoreSameVideoContent(video_content_amount, video_sample_id).execute();
                }
            }
        });


        Intent intent = getIntent();
        video_sample_id = intent.getIntExtra("video_sample_id", 0);

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(R.string.fa_caret_left);
        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        new GetSameVideoContent(video_content_amount, video_sample_id).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharePreferenceDB sharePreferenceDB = new SharePreferenceDB(this);
        ArrayList<Integer>clickFavoriteIdArrayList = sharePreferenceDB.getListInt("clickFavoriteIdArrayList");
        for(int i=0; i < sameVideoContentList.size(); i++){
           if(clickFavoriteIdArrayList.contains(sameVideoContentList.get(i).id)){
               for(int j=0; j < sameVideoContentList.get(i).likeList.size(); j++){
                   if(sharePreferenceDB.getInt("id") ==  sameVideoContentList.get(i).likeList.get(j).user_id){
                       sameVideoContentList.get(i).likeList.get(j).is_click = 1;
                       break;
                   }
               }
           }
        }

    }


    class GetSameVideoContent extends AsyncTask<String, String, String> {
        int video_content_amount;
        int video_sample_id;

        GetSameVideoContent(int video_content_amount, int video_sample_id){
            this.video_content_amount = video_content_amount;
            this.video_sample_id = video_sample_id;
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


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("video_content_amount", String.valueOf(video_content_amount)));
            params.add(new BasicNameValuePair("video_sample_id", String.valueOf(video_sample_id)));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray sameVideoContentJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "get_same_sample_video_content.php",
                    "POST", params);


            if(sameVideoContentJSONArray != null){
                // check log cat fro response
                Log.d("Create Response", sameVideoContentJSONArray.toString());

                try{
                    for(int i = 0; i < sameVideoContentJSONArray.length(); i++){
                        VideoContent videoContent = new VideoContent();
                        videoContent.id = sameVideoContentJSONArray.optJSONObject(i).optInt("id");
                        videoContent.videoSampleId = sameVideoContentJSONArray.optJSONObject(i).optInt("video_sample_id");
                        User user = new User();
                        JSONObject userJsonObject = sameVideoContentJSONArray.optJSONObject(i).optJSONObject("user");
                        user.id = userJsonObject.optInt("id");
                        user.name = userJsonObject.optString("name");
                        user.profile = userJsonObject.optString("profile");
                        videoContent.owner = user;
                        JSONArray likeJsonArray = sameVideoContentJSONArray.optJSONObject(i).optJSONArray("like");
                        List<Like>likeList = new ArrayList<>();
                        for(int j = 0; j < likeJsonArray.length(); j++){
                            Like like = new Like();
                            like.user_id =  likeJsonArray.optJSONObject(j).optInt("user_id");
                            like.is_click = likeJsonArray.optJSONObject(j).optInt("is_click");
                            likeList.add(like);
                        }
                        videoContent.likeList = likeList;
                        videoContent.likeAmount = likeList.size();
                        JSONArray commentJsonArray = sameVideoContentJSONArray.optJSONObject(i).optJSONArray("comment");
                        videoContent.commentAmount = commentJsonArray.length();
                        videoContent.title = sameVideoContentJSONArray.optJSONObject(i).optString("title");
                        videoContent.url =  Api.baseUrl + "video_content/" + sameVideoContentJSONArray.optJSONObject(i).optString("path");
                        sameVideoContentList.add(videoContent);
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
//            sameVideoContentPagerAdapter = new SameVideoContentPagerAdapter(SameVideoContentActivity.this, sameVideoContentList);
//            sameVideoContentVP.setAdapter(sameVideoContentPagerAdapter);

//            sameVideoContentAdapter = new SameVideoContentAdapter(SameVideoContentActivity.this, sameVideoContentList);
//            sameVideoContentRV.setAdapter(sameVideoContentAdapter);

            sameVideoContentAutoPlayAdapter.setSameVideoContentList(sameVideoContentList);
        }

    }

    class GetMoreSameVideoContent extends AsyncTask<String, String, String> {
        int video_content_amount;
        int video_sample_id;

        GetMoreSameVideoContent(int video_content_amount, int video_sample_id){
            this.video_content_amount = video_content_amount;
            this.video_sample_id = video_sample_id;
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


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("video_content_amount", String.valueOf(video_content_amount)));
            params.add(new BasicNameValuePair("video_sample_id", String.valueOf(video_sample_id)));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray sameVideoContentJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "get_same_sample_video_content.php",
                    "POST", params);


            if(sameVideoContentJSONArray != null){
                // check log cat fro response
                Log.d("Create Response", sameVideoContentJSONArray.toString());

                try{
                    for(int i = 0; i < sameVideoContentJSONArray.length(); i++){
                        VideoContent videoContent = new VideoContent();
                        videoContent.id = sameVideoContentJSONArray.optJSONObject(i).optInt("id");
                        videoContent.videoSampleId = sameVideoContentJSONArray.optJSONObject(i).optInt("video_sample_id");
                        User user = new User();
                        JSONObject userJsonObject = sameVideoContentJSONArray.optJSONObject(i).optJSONObject("user");
                        user.id = userJsonObject.optInt("id");
                        user.name = userJsonObject.optString("name");
                        user.profile = userJsonObject.optString("profile");
                        videoContent.owner = user;
                        JSONArray likeJsonArray = sameVideoContentJSONArray.optJSONObject(i).optJSONArray("like");
                        List<Like>likeList = new ArrayList<>();
                        for(int j = 0; j < likeJsonArray.length(); j++){
                            Like like = new Like();
                            like.user_id =  likeJsonArray.optJSONObject(j).optInt("user_id");
                            like.is_click = likeJsonArray.optJSONObject(j).optInt("is_click");
                            likeList.add(like);
                        }
                        videoContent.likeList = likeList;
                        videoContent.likeAmount = likeList.size();
                        JSONArray commentJsonArray = sameVideoContentJSONArray.optJSONObject(i).optJSONArray("comment");
                        videoContent.commentAmount = commentJsonArray.length();
                        videoContent.title = sameVideoContentJSONArray.optJSONObject(i).optString("title");
                        videoContent.url =  Api.baseUrl + "video_content/" + sameVideoContentJSONArray.optJSONObject(i).optString("path");
                        sameVideoContentList.add(videoContent);
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
//            sameVideoContentPagerAdapter = new SameVideoContentPagerAdapter(SameVideoContentActivity.this, sameVideoContentList);
//            sameVideoContentVP.setAdapter(sameVideoContentPagerAdapter);

//            sameVideoContentAdapter = new SameVideoContentAdapter(SameVideoContentActivity.this, sameVideoContentList);
//            sameVideoContentRV.setAdapter(sameVideoContentAdapter);
            current_size = sameVideoContentList.size();
            is_loading = false;
            sameVideoContentAutoPlayAdapter.setSameVideoContentList(sameVideoContentList);
        }

    }

    public void prepareAutoPlayRV(){
        //todo before setAdapter
        sameVideoContentAutoPlayRV.setActivity(SameVideoContentActivity.this);

        //optional - to play only first visible video
        sameVideoContentAutoPlayRV.setPlayOnlyFirstVideo(true); // false by default

        //optional - by default we check if url ends with ".mp4". If your urls do not end with mp4, you can set this param to false and implement your own check to see if video points to url
        sameVideoContentAutoPlayRV.setCheckForMp4(false); //true by default

        //optional - download videos to local storage (requires "android.permission.WRITE_EXTERNAL_STORAGE" in manifest or ask in runtime)
//        recyclerView.setDownloadPath(Environment.getExternalStorageDirectory() + "/MyVideo"); // (Environment.getExternalStorageDirectory() + "/Video") by default

//        recyclerView.setDownloadVideos(true); // false by default

        sameVideoContentAutoPlayRV.setVisiblePercent(50); // percentage of View that needs to be visible to start playing

        //call this functions when u want to start autoplay on loading async lists (eg firebase)
        sameVideoContentAutoPlayRV.smoothScrollBy(0,1);
        sameVideoContentAutoPlayRV.smoothScrollBy(0,-1);
    }

}
