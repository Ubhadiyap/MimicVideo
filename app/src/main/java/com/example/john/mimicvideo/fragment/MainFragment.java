package com.example.john.mimicvideo.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.john.mimicvideo.CameraActivity;
import com.example.john.mimicvideo.CommentActivity;
import com.example.john.mimicvideo.LoginActivity;
import com.example.john.mimicvideo.ProfileActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.SearchActivity;
import com.example.john.mimicvideo.ShowVideoContentActivity;
import com.example.john.mimicvideo.TestVideoActivity;
import com.example.john.mimicvideo.VideoSampleActivity;
import com.example.john.mimicvideo.adapter.MainVideoContentAutoPlayAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.Comment;
import com.example.john.mimicvideo.model.Like;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.model.VideoSample;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.example.john.mimicvideo.view.AutoPlayVideo.AAH_CustomRecyclerView;

import junit.framework.Test;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/3/19.
 */

public class MainFragment extends Fragment {
    TextView goProfilePageTxt;
    TextView goVideoSamplePageTxt;
    TextView searchIconTxt, searchNameTxt;
    JSONParser jsonParser = new JSONParser();
    ViewPager mainVideoContentVP;
    RecyclerView mainVideoContentRV;
    AAH_CustomRecyclerView mainVideoContentAutoPlayRV;
    List<VideoContent>videoContentList = new ArrayList<>();
    SharePreferenceDB sharePreferenceDB;
    private int video_content_amount = 20;
    private int current_size = 100;
    private boolean is_loading = false;
    public MainVideoContentAutoPlayAdapter mainVideoContentAutoPlayAdapter;

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
//        if(videoContentPlayerView.getPlayer() != null){
//            videoContentPlayerView.getPlayer().setPlayWhenReady(false);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mainVideoContentAutoPlayRV.playAvailableVideos(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(videoContentPlayerView.getPlayer() != null){
//            videoContentPlayerView.getPlayer().release();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mainVideoContentAutoPlayRV.stopVideos();
    }


    public static MainFragment newInstance(){
        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();
        mainFragment.setArguments(bundle);
        return mainFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_main, null);
        ApplicationService.verifyStoragePermissions(getActivity());

        goProfilePageTxt = contentView.findViewById(R.id.goProfilePageTxt);
        goVideoSamplePageTxt = contentView.findViewById(R.id.goVideoSamplePageTxt);
        searchIconTxt = contentView.findViewById(R.id.searchIconTxt);
        searchNameTxt = contentView.findViewById(R.id.searchTitleTxt);
        mainVideoContentVP = contentView.findViewById(R.id.mainVideoContentVP);
        mainVideoContentRV = contentView.findViewById(R.id.mainVideoContentRV);
        mainVideoContentAutoPlayRV = contentView.findViewById(R.id.mainVideoContentAutoPlayRV);

        sharePreferenceDB = new SharePreferenceDB(getActivity());


        goProfilePageTxt.setTypeface(ApplicationService.getFont());
        goProfilePageTxt.setText(getText(R.string.fa_user_o));

        goVideoSamplePageTxt.setTypeface(ApplicationService.getFont());
        goVideoSamplePageTxt.setText(getString(R.string.fa_plus_square_o));

        searchIconTxt.setTypeface(ApplicationService.getFont());
        searchIconTxt.setText(getString(R.string.fa_search));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mainVideoContentAutoPlayRV.setLayoutManager(layoutManager);
        mainVideoContentAutoPlayAdapter = new MainVideoContentAutoPlayAdapter(getActivity(), videoContentList);
        mainVideoContentAutoPlayRV.setAdapter(mainVideoContentAutoPlayAdapter);
        prepareAutoPlayRV();

        mainVideoContentAutoPlayRV.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                super.onScrollStateChanged(recyclerView, scrollState);

                if(((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() > (current_size - 50) && !is_loading){
                    is_loading = true;
                    video_content_amount = video_content_amount + 100;
                    new GetAllMoreVideoContent(video_content_amount).execute();
                }
            }
        });



        goProfilePageTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharePreferenceDB.getInt("id") != 0 ){
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), ProfileActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        goVideoSamplePageTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent();
               intent.setClass(getActivity(), VideoSampleActivity.class);
               startActivity(intent);
            }
        });

        searchIconTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        new GetAllVideoContent(video_content_amount).execute();
        return contentView;
    }

    public void prepareAutoPlayRV(){
        //todo before setAdapter
        mainVideoContentAutoPlayRV.setActivity(getActivity());

        //optional - to play only first visible video
        mainVideoContentAutoPlayRV.setPlayOnlyFirstVideo(true); // false by default

        //optional - by default we check if url ends with ".mp4". If your urls do not end with mp4, you can set this param to false and implement your own check to see if video points to url
        mainVideoContentAutoPlayRV.setCheckForMp4(false); //true by default

        //optional - download videos to local storage (requires "android.permission.WRITE_EXTERNAL_STORAGE" in manifest or ask in runtime)
//        recyclerView.setDownloadPath(Environment.getExternalStorageDirectory() + "/MyVideo"); // (Environment.getExternalStorageDirectory() + "/Video") by default

//        recyclerView.setDownloadVideos(true); // false by default

        mainVideoContentAutoPlayRV.setVisiblePercent(50); // percentage of View that needs to be visible to start playing

        //call this functions when u want to start autoplay on loading async lists (eg firebase)
        mainVideoContentAutoPlayRV.smoothScrollBy(0,1);
        mainVideoContentAutoPlayRV.smoothScrollBy(0,-1);
    }

    class GetAllVideoContent extends AsyncTask<String, String, String> {
        int video_content_amount;

        GetAllVideoContent(int video_content_amount){
            this.video_content_amount = video_content_amount;
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

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray videoContentJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "get_all_video_content.php",
                    "POST", params);

            // check log cat fro response
            if(videoContentJSONArray != null){
                Log.d("Create Response", videoContentJSONArray.toString());

                videoContentList.clear();

                try{
                    for(int i = 0; i < videoContentJSONArray.length(); i++){
                        VideoContent videoContent = new VideoContent();
                        videoContent.id = videoContentJSONArray.optJSONObject(i).optInt("id");
                        videoContent.videoSampleId = videoContentJSONArray.optJSONObject(i).optInt("video_sample_id");
                        videoContent.videoSampleUrl = videoContentJSONArray.optJSONObject(i).optString("video_sample_path5 ");
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
                        videoContent.url =  Api.videoContentUrl + videoContentJSONArray.optJSONObject(i).optString("path");
                        videoContentList.add(videoContent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            return null;
        }


        protected void onPostExecute(String file_url) {
            is_loading = false;
            mainVideoContentAutoPlayAdapter.setMainVideoContentList(videoContentList);
        }


    }


    class GetAllMoreVideoContent extends AsyncTask<String, String, String> {
        int video_content_amount;

        GetAllMoreVideoContent(int video_content_amount){
            this.video_content_amount = video_content_amount;
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

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray videoContentJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "get_all_video_content.php",
                    "POST", params);

            // check log cat fro response
            if(videoContentJSONArray != null){
                Log.d("Create Response", videoContentJSONArray.toString());

                videoContentList.clear();

                try{
                    for(int i = 0; i < videoContentJSONArray.length(); i++){
                        VideoContent videoContent = new VideoContent();
                        videoContent.id = videoContentJSONArray.optJSONObject(i).optInt("id");
                        videoContent.videoSampleId = videoContentJSONArray.optJSONObject(i).optInt("video_sample_id");
                        videoContent.videoSampleUrl = videoContentJSONArray.optJSONObject(i).optString("video_sample_path5 ");
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
                        videoContent.url =  Api.videoContentUrl + videoContentJSONArray.optJSONObject(i).optString("path");
                        videoContentList.add(videoContent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            return null;
        }


        protected void onPostExecute(String file_url) {
            current_size = videoContentList.size();
            is_loading = false;
            mainVideoContentAutoPlayAdapter.setMainVideoContentList(videoContentList);
        }


    }
}
