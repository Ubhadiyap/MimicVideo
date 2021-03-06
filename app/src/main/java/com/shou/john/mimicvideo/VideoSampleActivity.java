package com.shou.john.mimicvideo;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.john.mimicvideo.R;
import com.shou.john.mimicvideo.adapter.VideoSampleAdapter;
import com.shou.john.mimicvideo.api.Api;
import com.shou.john.mimicvideo.model.VideoSample;
import com.shou.john.mimicvideo.utils.ApplicationService;
import com.shou.john.mimicvideo.utils.GridLayoutManagerWithSmoothScroller;
import com.shou.john.mimicvideo.utils.JSONParser;
import com.shou.john.mimicvideo.utils.SearchItemDecoration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class VideoSampleActivity extends BaseActivity {
    private String TAG = VideoSampleActivity.class.getSimpleName();
    JSONParser jsonParser = new JSONParser();
    VideoSampleAdapter videoSampleAdapter;
    List<VideoSample> videoSampleList = new ArrayList<>();
    private RecyclerView videoSampleRV;
    private ImageView toCameraImg;

    private boolean is_loading = false;
    private int video_sample_amount = 100;
    private TextView backTxt;
    private boolean first = true;
    private EditText searchVideoContentEdit;
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_sample);
        videoSampleRV = findViewById(R.id.videoSampleRV);
        backTxt = findViewById(R.id.backTxt);
        searchVideoContentEdit = findViewById(R.id.searchVideoSampleEdit);
        toCameraImg = findViewById(R.id.toCameraImg);

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(getString(R.string.fa_angle_left));

        GridLayoutManagerWithSmoothScroller layoutManager = new GridLayoutManagerWithSmoothScroller(VideoSampleActivity.this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        videoSampleAdapter = new VideoSampleAdapter(VideoSampleActivity.this, videoSampleList);
        videoSampleRV.setLayoutManager(layoutManager);
        videoSampleRV.setAdapter(videoSampleAdapter);



        new GetVideoSample(video_sample_amount).execute();

        videoSampleRV.addItemDecoration(new SearchItemDecoration(VideoSampleActivity.this, 0));
//        videoSampleRV.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
//                super.onScrollStateChanged(recyclerView, scrollState);
//
//                if(((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() > (current_size - 50) && !is_loading){
//
//                    is_loading = true;
//                    video_sample_amount = video_sample_amount + 100;
//                    new SearchVideoSample(searchVideoContentEdit.getText().toString(),video_sample_amount).execute();
//                }
//            }
//        });

        searchVideoContentEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    video_sample_amount = 100;
                    new GetVideoSample(video_sample_amount).execute();
                }else{
                    video_sample_amount = 100;
                    new SearchVideoSample(searchVideoContentEdit.getText().toString(), video_sample_amount).execute();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toCameraImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(VideoSampleActivity.this, ContinueCameraActivity.class);
                startActivity(intent);
            }
        });
    }

    class GetVideoSample extends AsyncTask<String, String, String> {
        int video_sample_amount;

        public GetVideoSample(int video_sample_amount){
            this.video_sample_amount = video_sample_amount;
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
            params.add(new BasicNameValuePair("video_sample_amount", String.valueOf(video_sample_amount)));
            // getting JSON string from URL
            JSONArray videoSampleJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "get_video_sample.php", "POST", params);


            if(videoSampleJSONArray != null){
                videoSampleList.clear();
                for(int i = 0; i < videoSampleJSONArray.length(); i++){
                    VideoSample videoSample = new VideoSample();
                    videoSample.id = videoSampleJSONArray.optJSONObject(i).optInt("id");
                    videoSample.title = videoSampleJSONArray.optJSONObject(i).optString("title");
                    videoSample.url = Api.videoSampleUrl + videoSampleJSONArray.optJSONObject(i).optString("path");
                    videoSampleList.add(videoSample);
                }

                Log.d(TAG, "create jsonArray " + videoSampleJSONArray.toString());
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

            videoSampleAdapter.setVideoSampleList(videoSampleList);
            is_loading = false;
        }

    }

    class SearchVideoSample extends AsyncTask<String, String, String> {
        String searchWord;
        int video_sample_amount;

        public SearchVideoSample(String searchWord, int video_sample_amount){
            this.searchWord = searchWord;
            this.video_sample_amount = video_sample_amount;
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
            params.add(new BasicNameValuePair("searchWord", searchWord));
            params.add(new BasicNameValuePair("video_sample_amount", String.valueOf(video_sample_amount)));
            // getting JSON string from URL
            JSONArray videoSampleJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "search_video_sample.php", "POST", params);

            if(videoSampleJSONArray != null){
                videoSampleList.clear();
                for(int i = 0; i < videoSampleJSONArray.length(); i++){
                    videoSampleList.clear();
                    VideoSample videoSample = new VideoSample();
                    videoSample.id = videoSampleJSONArray.optJSONObject(i).optInt("id");
                    videoSample.title = videoSampleJSONArray.optJSONObject(i).optString("title");
                    videoSample.url = Api.videoSampleUrl + videoSampleJSONArray.optJSONObject(i).optString("path");
                    videoSampleList.add(videoSample);
                }

                // Check your log cat for JSON reponse
                Log.d("VideoSample data: ", videoSampleJSONArray.toString());
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // updating UI from Background Thread
            videoSampleAdapter.setVideoSampleList(videoSampleList);
            is_loading = false;
        }

    }
}
