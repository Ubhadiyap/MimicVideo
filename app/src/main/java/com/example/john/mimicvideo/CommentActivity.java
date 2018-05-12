package com.example.john.mimicvideo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.john.mimicvideo.adapter.CommentAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.Comment;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends BaseActivity {
    private RecyclerView commentRV;
    private EditText createCommentEdit;
    private TextView backTxt;
    private ImageView createCommentImg;
    private JSONParser jsonParser = new JSONParser();
    private List<Comment>commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private int video_content_id;
    private int user_id;
    private int comment_amount = 100;
    private SharePreferenceDB sharePreferenceDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        commentRV = findViewById(R.id.commentRV);
        createCommentEdit = findViewById(R.id.createCommentEdit);
        createCommentImg = findViewById(R.id.createCommentImg);
        backTxt = findViewById(R.id.backTxt);
        sharePreferenceDB = new SharePreferenceDB(this);

        video_content_id = getIntent().getIntExtra("video_content_id", 0);
        user_id = sharePreferenceDB.getInt("id");

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(getString(R.string.fa_angle_left));
        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        createCommentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_id != 0){
                    if(!createCommentEdit.getText().toString().trim().equals("")){
                        new CreateComment(user_id, video_content_id, createCommentEdit.getText().toString()).execute();
                        createCommentEdit.setText("");
                        hideSoftKeyboard(CommentActivity.this, createCommentEdit);
                    }else{
                        Toast.makeText(CommentActivity.this, "開嘴怎能沒話", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setClass(CommentActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        new GetComment(video_content_id, comment_amount).execute();


    }

    class GetComment extends AsyncTask<String, String, String> {
        int video_content_id;
        int comment_amount;

        GetComment(int video_content_id, int comment_amount){
            this.video_content_id = video_content_id;
            this.comment_amount = comment_amount;
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

            params.add(new BasicNameValuePair("video_content_id", String.valueOf(video_content_id)));
            params.add(new BasicNameValuePair("comment_amount", String.valueOf(comment_amount)));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONArray commentJSONArray = jsonParser.makeHttpRequestArray(Api.baseUrl + "get_comment.php",
                    "POST", params);

            if(commentJSONArray != null){
                try{
                    for(int i = 0; i < commentJSONArray.length(); i++){
                        Comment comment = new Comment();
                        User user = new User();
                        JSONObject userJsonObject = commentJSONArray.optJSONObject(i).optJSONObject("user");
                        user.id = userJsonObject.optInt("id");
                        user.name = userJsonObject.optString("name");
                        user.profile = userJsonObject.optString("profile");
                        comment.content = commentJSONArray.optJSONObject(i).optString("content");
                        comment.owner = user;
                        commentList.add(comment);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

//                check log cat fro response
                Log.d("Create Response", commentJSONArray.toString());
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            commentAdapter = new CommentAdapter(CommentActivity.this, commentList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(CommentActivity.this, LinearLayoutManager.VERTICAL, false);
            commentRV.setLayoutManager(layoutManager);
            DividerItemDecoration itemDecor = new DividerItemDecoration(CommentActivity.this, DividerItemDecoration.VERTICAL);
            commentRV.addItemDecoration(itemDecor);
            commentRV.setAdapter(commentAdapter);
        }

    }

    class CreateComment extends AsyncTask<String, String, JSONObject> {
        int user_id;
        int video_content_id;
        String content;

        CreateComment(int user_id, int video_content_id, String content){
            this.user_id = user_id;
            this.video_content_id = video_content_id;
            this.content = content;
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
            params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
            params.add(new BasicNameValuePair("video_content_id", String.valueOf(video_content_id)));
            params.add(new BasicNameValuePair("content", content));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonObject = jsonParser.makeHttpRequest(Api.baseUrl + "create_comment.php",
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
                    Comment comment = new Comment();
                    User user = new User();
                    user.id = sharePreferenceDB.getInt("id");
                    user.name = sharePreferenceDB.getString("name");
                    user.profile = sharePreferenceDB.getString("profile");
                    comment.content = content;
                    comment.owner = user;
                    commentAdapter.addNewComment(comment);

                    commentAdapter.notifyDataSetChanged();
                    commentRV.smoothScrollToPosition(commentAdapter.getItemCount());
                }else{
                    Toast.makeText(CommentActivity.this, "留言失敗，請重新留言", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(CommentActivity.this, "留言失敗，請重新留言", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
