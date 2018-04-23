package com.example.john.mimicvideo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.example.john.mimicvideo.view.TextDrawable;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends BaseActivity {
    private String TAG = LoginActivity.class.getSimpleName();
    // FB
    private LoginManager loginManager;
    private CallbackManager callbackManager;

    private Button login_facebook, fbLogoutBtn;
    private JSONParser jsonParser = new JSONParser();
    private SharePreferenceDB sharePreferenceDB ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        fbLogoutBtn = findViewById(R.id.fbLlogoutBtn);

        login_facebook = findViewById(R.id.login_facebook);
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .useFont(ApplicationService.getFont())
                .fontSize((int)getResources().getDimension(R.dimen.facebook_text_size)) /* size in px */
                .bold()
                .endConfig()
                .buildRect(getString(R.string.fa_facebook), getResources().getColor(R.color.com_facebook_button_background_color));

        sharePreferenceDB = new SharePreferenceDB(this);

        login_facebook.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        login_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //fbBtn.setEnabled(false);

                if(AccessToken.getCurrentAccessToken() == null){
                    LoginManager.getInstance().registerCallback(callbackManager,
                            new FacebookCallback<LoginResult>() {
                                @Override
                                public void onSuccess(LoginResult loginResult) {
                                    System.out.println("fb login successfully");

                                    GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            try {
                                                if (response.getConnection().getResponseCode() == 200) {
                                                    long id = object.getLong("id");
                                                    String name = object.getString("name");
//                                                    String email = object.getString("email");
                                                    Log.d(TAG, "Facebook id:" + id);
                                                    Log.d(TAG, "Facebook name:" + name);
//                                                    Log.d(TAG, "Facebook email:" + email);
                                                    // 此時如果登入成功，就可以順便取得用戶大頭照
//                                                    Profile profile = Profile.getCurrentProfile();
                                                    // 設定大頭照大小
//                                                    Uri userPhoto = profile.getProfilePictureUri(300, 300);
                                                    new CreateNewUser(object).execute();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    // https://developers.facebook.com/docs/android/graph?locale=zh_TW
                                    // 如果要取得email，需透過添加參數的方式來獲取(如下)
                                    // 不添加只能取得id & name
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields", "id,name,email");
                                    graphRequest.setParameters(parameters);
                                    graphRequest.executeAsync();

                                }

                                @Override
                                public void onCancel() {
                                    System.out.println("fb login cancel");
                                    login_facebook.setEnabled(true);
                                }

                                @Override
                                public void onError(FacebookException exception) {
                                    login_facebook.setEnabled(true);
                                }
                            });

                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
                }
                else{
                    System.out.println("CurrentAccessToken : "+ AccessToken.getCurrentAccessToken().getToken());
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, ProfileActivity.class);
                    startActivity(intent);

                }
            }
        });

        fbLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginManager.logOut();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    class CreateNewUser extends AsyncTask<String, String, String> {
        JSONObject jsonObject;
        String profile;

        CreateNewUser(JSONObject jsonObject){
            this.jsonObject = jsonObject;
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

            String id = jsonObject.optString("id");
            profile = "https://graph.facebook.com/" + id + "/picture?type=large";
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", jsonObject.optString("id")));
            params.add(new BasicNameValuePair("password", "fb"));
            params.add(new BasicNameValuePair("name", jsonObject.optString("name")));
            params.add(new BasicNameValuePair("profile", profile));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(Api.baseUrl + "create_user.php",
                    "POST", params);

            // check log cat fro response
            if(json != null){
                Log.d("Create Response", json.toString());

                if(json.optInt("success") == 1){
                    sharePreferenceDB.putInt("id", json.optInt("id"));
                    sharePreferenceDB.putString("name",  jsonObject.optString("name"));
                    sharePreferenceDB.putString("profile", profile);
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Log.d(TAG, "登入失敗1，請重新登入");
                    loginManager.logOut();
                }
            }else{
                Log.d(TAG, "登入失敗2，請重新登入");
                loginManager.logOut();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
        }

    }
}
