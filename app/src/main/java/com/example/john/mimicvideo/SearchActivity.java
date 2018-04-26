package com.example.john.mimicvideo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.john.mimicvideo.adapter.SearchVideoContentAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.model.VideoContent;
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

public class SearchActivity extends BaseActivity {
    TextView backTxt;
    EditText keyWordEdit;
    private TextView clearTxt;
    JSONParser jsonParser = new JSONParser();
    List<VideoContent>searchVideoContentList = new ArrayList<>();
    private int LOAD_TESTING_VIDEO = 3;
    private RecyclerView searchVideoContentRV;
    private SearchVideoContentAdapter searchVideoContentAdapter;
    private GridLayoutManagerWithSmoothScroller layoutManager;;
    private int video_content_amount = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        backTxt = findViewById(R.id.backTxt);
        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(getString(R.string.fa_caret_left));

        keyWordEdit = findViewById(R.id.keyWordEdit);
        searchVideoContentRV = findViewById(R.id.searchVideoContentRV);
        clearTxt = findViewById(R.id.clearTxt);
        clearTxt.setTypeface(ApplicationService.getFont());
        clearTxt.setText(R.string.fa_times);

        layoutManager = new GridLayoutManagerWithSmoothScroller(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
               return 1;
            }
        });

        searchVideoContentAdapter = new SearchVideoContentAdapter(this, searchVideoContentList);
        searchVideoContentRV.setAdapter(searchVideoContentAdapter);
        searchVideoContentRV.setLayoutManager(layoutManager);
        searchVideoContentRV.addItemDecoration(new SearchItemDecoration(this, 0));



        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        clearTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyWordEdit.setText("");
            }
        });

        keyWordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    clearTxt.setVisibility(View.INVISIBLE);
                    searchVideoContentList = new ArrayList<>();
                    searchVideoContentAdapter.resetSearch(searchVideoContentList);
                }else{
                    clearTxt.setVisibility(View.VISIBLE);
                    video_content_amount = 20;
                    new SearchVideoContent(keyWordEdit.getText().toString(), video_content_amount).execute();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == LOAD_TESTING_VIDEO) {
                // Get the Video from data
                Uri selectedVideo = data.getData();
                String[] filePathColumn = {MediaStore.Video.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedVideo, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
                    String mediaPath = cursor.getString(columnIndex);
                    Toast.makeText(this, mediaPath, Toast.LENGTH_SHORT).show();
                    //uptestVideoName is a EditText
                    cursor.close();
                }
            }
        }
    }

    class SearchVideoContent extends AsyncTask<String, String, String> {
        String keyWord;
        int video_content_amount;

        SearchVideoContent(String keyWord, int video_content_amount){
           this.keyWord = keyWord;
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
            params.add(new BasicNameValuePair("searchWord", keyWord));
            params.add(new BasicNameValuePair("video_content_amount", String.valueOf(video_content_amount)));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray searchVideoContentJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "search_video_content.php",
                    "POST", params);

            if(searchVideoContentJSONArray != null){
                // check log cat fro response
                Log.d("Create Response", searchVideoContentJSONArray.toString());
                searchVideoContentList.clear();

                try{
                    for(int i = 0; i < searchVideoContentJSONArray.length(); i++){
                        VideoContent videoContent = new VideoContent();
                        videoContent.id = searchVideoContentJSONArray.optJSONObject(i).optInt("id");
                        videoContent.videoSampleId = searchVideoContentJSONArray.optJSONObject(i).optInt("video_sample_id");
                        User user = new User();
                        user.id = searchVideoContentJSONArray.optJSONObject(i).optInt("user_id");
                        user.name = searchVideoContentJSONArray.optJSONObject(i).optString("name");
                        user.profile = searchVideoContentJSONArray.optJSONObject(i).optString("profile");
                        videoContent.owner = user;
                        videoContent.likeAmount = searchVideoContentJSONArray.optJSONObject(i).optInt("like_amount");
                        videoContent.title = searchVideoContentJSONArray.optJSONObject(i).optString("title");
                        videoContent.url =  Api.baseUrl + "video_content/" + searchVideoContentJSONArray.optJSONObject(i).optString("path");
                        searchVideoContentList.add(videoContent);
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
            searchVideoContentAdapter.resetSearch(searchVideoContentList);
        }

    }
}
