package com.example.john.mimicvideo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.john.mimicvideo.adapter.MainVideoContentAutoPlayAdapter;
import com.example.john.mimicvideo.adapter.ReportDescriptionAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.Like;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditVideoContentActivity extends BaseActivity {
    private String TAG = EditVideoContentActivity.class.getSimpleName();
    private SimpleExoPlayerView videoContentPlayerView;
    TextView backTxt;
    TextView deleteTxt;
    EditText editTitleEdit;
    Button saveBtn;
    private ImageView openCommentImg;
    private ImageView giveLikeImg;
    private TextView commentAmountTxt;
    private TextView likeAmountTxt;
    private int user_id;

    private JSONParser jsonParser = new JSONParser();
    private VideoContent editVideoContent ;
    private int videoContentId;

    private SharePreferenceDB sharePreferenceDB;
    private ArrayList<String>clickFavoriteIdArrayList = new ArrayList<>();
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
        if(videoContentPlayerView.getPlayer() != null){
            videoContentPlayerView.getPlayer().setPlayWhenReady(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        if(videoContentPlayerView.getPlayer() != null){
            videoContentPlayerView.getPlayer().setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoContentPlayerView.getPlayer() != null){
            videoContentPlayerView.getPlayer().release();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video_content);
        videoContentPlayerView = findViewById(R.id.videoContentPlayerView);
        backTxt = findViewById(R.id.backTxt);
        deleteTxt = findViewById(R.id.deleteTxt);
        editTitleEdit = findViewById(R.id.editTitleEdit);
        saveBtn = findViewById(R.id.saveBtn);
        openCommentImg = findViewById(R.id.openCommentImg);
        giveLikeImg = findViewById(R.id.giveLikeImg);
        commentAmountTxt = findViewById(R.id.commentAmountTxt);
        likeAmountTxt = findViewById(R.id.likeAmountTxt);

        sharePreferenceDB = new SharePreferenceDB(this);
        user_id = sharePreferenceDB.getInt("id");
        clickFavoriteIdArrayList = sharePreferenceDB.getListString("clickFavoriteIdArrayList");

        videoContentId = getIntent().getIntExtra("videoContentId", 0);


        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(R.string.fa_caret_left);
        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        deleteTxt.setTypeface(ApplicationService.getFont());
        deleteTxt.setText(R.string.fa_times);
        deleteTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(EditVideoContentActivity.this, R.style.selectorDialog);
                dialog.setContentView(R.layout.dialog_delete_video);

                TextView yesBtn = dialog.findViewById(R.id.yesBtn);
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(editVideoContent != null){
                            new DeleteVideoContent(editVideoContent.id).execute();
                            finish();
                        }else{
                            Toast.makeText(EditVideoContentActivity.this, "發生錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                TextView noBtn = dialog.findViewById(R.id.noBtn);
                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });


                // 由程式設定 Dialog 視窗外的明暗程度, 亮度從 0f 到 1f
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount = 0.5f;
                dialog.getWindow().setAttributes(lp);
                dialog.show();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editVideoContent != null){
                    new EditVideoContent(editVideoContent.id,  editTitleEdit.getText().toString()).execute();
                    Intent intent = new Intent(EditVideoContentActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(EditVideoContentActivity.this, "發生錯誤", Toast.LENGTH_SHORT).show();
                }
            }
        });

        openCommentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editVideoContent != null){
                    Intent intent = new Intent();
                    intent.putExtra("video_content_id", editVideoContent.id);
                    intent.setClass(EditVideoContentActivity.this, CommentActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(EditVideoContentActivity.this, "發生錯誤", Toast.LENGTH_SHORT).show();
                }
            }
        });

        giveLikeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editVideoContent != null){
                    if(sharePreferenceDB.getInt("id") != 0){
                        if(((ImageView)view).getDrawable().getCurrent().getConstantState()== getResources().getDrawable(R.drawable.smile_like_yellow).getConstantState()){
                            ((ImageView)view).setImageResource(R.drawable.smile_like);
                            new UpdateLikeAmount(user_id, editVideoContent.id, 0).execute();
                            editVideoContent.likeAmount = editVideoContent.likeAmount - 1;
                            likeAmountTxt.setText(String.valueOf(editVideoContent.likeAmount));
                            clickFavoriteIdArrayList.remove(String.valueOf(editVideoContent.id));
                            sharePreferenceDB.putListString("clickFavoriteIdArrayList", clickFavoriteIdArrayList);
                        }else{
                            ((ImageView)view).setImageResource(R.drawable.smile_like_yellow);
                            new UpdateLikeAmount(user_id, editVideoContent.id, 1).execute();
                            editVideoContent.likeAmount = editVideoContent.likeAmount + 1;
                            likeAmountTxt.setText(String.valueOf(editVideoContent.likeAmount));
                            clickFavoriteIdArrayList.add(String.valueOf(editVideoContent.id));
                            sharePreferenceDB.putListString("clickFavoriteIdArrayList", clickFavoriteIdArrayList);
                        }
                    }else{
                        Intent intent = new Intent();
                        intent.setClass(EditVideoContentActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(EditVideoContentActivity.this, "發生錯誤", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final GestureDetector gestureDetector = new
                GestureDetector(EditVideoContentActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (videoContentPlayerView.getPlayer().getPlayWhenReady()) {
                            videoContentPlayerView.getPlayer().setPlayWhenReady(false);
                        } else {
                            videoContentPlayerView.getPlayer().setPlayWhenReady(true);
                        }
                        return true;
                    }
                }
        );


        videoContentPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, @NonNull MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        new GetVideoContent(videoContentId).execute();
    }

    public void showVideo(){
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
        player = ExoPlayerFactory.newSimpleInstance(EditVideoContentActivity.this,trackSelector,loadControl);
        // 将player关联到View上
        videoContentPlayerView.setPlayer(player);
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(EditVideoContentActivity.this,
                Util.getUserAgent(EditVideoContentActivity.this, "yourApplicationName"), bandwidthMeter2);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource;
        // test hls
        //url="http://hls.videocc.net/ce0812b122/c/ce0812b122c492470605bc47d3388a09_3.m3u8";
        if(editVideoContent.url.contains(".m3u8")){
//            videoSource =new HlsMediaSource(Uri.parse(url),dataSourceFactory,null,null);
            videoSource = new ExtractorMediaSource(Uri.parse(editVideoContent.url),
                    dataSourceFactory, extractorsFactory, null, null);
        }else{
            //test mp4
            videoSource = new ExtractorMediaSource(Uri.parse(editVideoContent.url),
                    dataSourceFactory, extractorsFactory, null, null);
        }

        player.prepare(videoSource);
        player.setPlayWhenReady(true);
    }



    class EditVideoContent extends AsyncTask<String, String, String> {
        int id;
        String title;

        EditVideoContent(int id, String title){
            this.id = id;
            this.title = title;
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
            params.add(new BasicNameValuePair("id", String.valueOf(id)));
            params.add(new BasicNameValuePair("title", title));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest("http://1.34.63.239/update_video_content.php",
                    "POST", params);

            if(json != null){
                // check log cat fro response
                Log.d("Create Response", json.toString());
            }

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
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        }

    }

    class DeleteVideoContent extends AsyncTask<String, String, String> {
        int id;

        DeleteVideoContent(int id){
            this.id = id;
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
            params.add(new BasicNameValuePair("id", String.valueOf(id)));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest("http://1.34.63.239/delete_video_content.php",
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

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
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        }

    }

    class UpdateLikeAmount extends AsyncTask<String, String, String> {
        int user_id;
        int video_content_id;
        int is_click;

        public UpdateLikeAmount(int user_id, int video_content_id, int is_click) {
            this.user_id = user_id;
            this.video_content_id = video_content_id;
            this.is_click = is_click;
        }

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
            params.add(new BasicNameValuePair("video_content_id", String.valueOf(video_content_id)));
            params.add(new BasicNameValuePair("is_click", String.valueOf(is_click)));
            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest("http://1.34.63.239/give_video_content_like.php", "POST", params);

            if (json != null) {
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
    }

    class GetVideoContent extends AsyncTask<String, String, String> {
        int video_content_id;

        public GetVideoContent(int video_content_id) {
            this.video_content_id = video_content_id;
        }

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("video_content_id", String.valueOf(video_content_id)));

            JSONArray videoContentJSONArray = jsonParser.makeHttpRequestArray("http://1.34.63.239/get_video_content.php",
                    "POST", params);

            Log.d("Create Response", videoContentJSONArray.toString());

            try{
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
                    editVideoContent = videoContent;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {

            showVideo();

            commentAmountTxt.setText(String.valueOf(editVideoContent.commentAmount));
            likeAmountTxt.setText(String.valueOf(editVideoContent.likeList.size()));


            for(int i = 0; i < editVideoContent.likeList.size(); i++){
                if(editVideoContent.likeList.get(i).user_id == user_id){
                    if(editVideoContent.likeList.get(i).is_click == 1){
                        giveLikeImg.setImageResource(R.drawable.smile_like_yellow);
                    }else{
                        giveLikeImg.setImageResource(R.drawable.smile_like);
                    }
                }else{
                    giveLikeImg.setImageResource(R.drawable.smile_like);
                }
            }
        }
    }
}
