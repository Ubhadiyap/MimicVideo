package com.example.john.mimicvideo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.example.john.mimicvideo.adapter.CommentAdapter;
import com.example.john.mimicvideo.api.Api;
import com.example.john.mimicvideo.model.Comment;
import com.example.john.mimicvideo.model.User;
import com.example.john.mimicvideo.utils.ApplicationParameter;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.MediaUtil;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareMessengerMediaTemplateContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareDialog;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.john.mimicvideo.utils.FilePath.getDataColumn;
import static com.example.john.mimicvideo.utils.FilePath.isDownloadsDocument;
import static com.example.john.mimicvideo.utils.FilePath.isExternalStorageDocument;
import static com.example.john.mimicvideo.utils.FilePath.isGooglePhotosUri;
import static com.example.john.mimicvideo.utils.FilePath.isMediaDocument;

public class ShareActivity extends BaseActivity {
    private String TAG = ShareActivity.class.getSimpleName();
    TextView backTxt;
    TextView postVideoContentTxt;
    private JSONParser jsonParser = new JSONParser();
    private SharePreferenceDB sharePreferenceDB;
    private ImageView FBBtn, messageBtn, IGBtn, lineBtn, weiboBtn, wechatBtn, youTubeBtn, otherBtn;
    private int user_id;

    private CallbackManager callbackManager;

    private ShareDialog shareDialog;
    private MessageDialog msgDialog;


    private int FB_SHARE = 1;
    private int MESSAGE_SHARE = 2;
    private int IG_SHARE = 3;
    private int LINE_SHARE = 4;
    private int WEIBO_SHARE = 5;
    private int WECHAT_SHARE = 6;
    private int YOUTUBE = 7;
    private int OTHER_SHARE = 8;
    private int POST = 9;

    private String videoContentTitle = "";

    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        backTxt = findViewById(R.id.backTxt);
        postVideoContentTxt = findViewById(R.id.postVideoContentTxt);
        FBBtn = findViewById(R.id.FBBtn);
        messageBtn = findViewById(R.id.messageBtn);
        IGBtn = findViewById(R.id.IGBtn);
        lineBtn = findViewById(R.id.lineBtn);
        weiboBtn = findViewById(R.id.weiboBtn);
        wechatBtn = findViewById(R.id.wechatBtn);
        youTubeBtn = findViewById(R.id.youtubeBtn);
        otherBtn = findViewById(R.id.otherBtn);

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(R.string.fa_angle_left);

        // Initialize facebook SDK.
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Create a callbackManager to handle the login responses.
        callbackManager = CallbackManager.Factory.create();

        shareDialog = new ShareDialog(this);

        // this part is optional
        shareDialog.registerCallback(callbackManager, callback);


        msgDialog = new MessageDialog(this);
        msgDialog.registerCallback(callbackManager, messenger_callback);


        sharePreferenceDB = new SharePreferenceDB(this);
        user_id = sharePreferenceDB.getInt("id");

        if(getIntent().getStringExtra("videoContentTitle") != null){
            postVideoContentTxt.setEnabled(true);
            final String videoContentTitle = getIntent().getStringExtra("videoContentTitle");
            final int videoSampleId = getIntent().getIntExtra("videoSampleId", 0);
            postVideoContentTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(ShareActivity.this, LoadingActivity.class);
                    intent.putExtra("videoContentTitle", videoContentTitle);
                    intent.putExtra("videoSampleId", videoSampleId);
                    startActivity(intent);


                    //below for test

//                    File file = new File(ApplicationParameter.FILE_SAVE_PATH);
//                    String fileName = UUID.randomUUID().toString() + "_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".mp4";
//                    new PostVideoContent(user_id, 1, videoContentTitle, fileName).execute();
//                    new BackgroundUploader(ShareActivity.this, Api.baseUrl + "upload_video_content_file.php", file, fileName).execute();
//                    ApplicationService.verifyStoragePermissions(ShareActivity.this);
//                    Intent intent = new Intent();
//                    intent.setType("video/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), POST);
                }
            });
        }else{
            postVideoContentTxt.setBackground(getResources().getDrawable(R.drawable.layout_border_not_post_textview));
            postVideoContentTxt.setEnabled(false);
        }

        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApplicationService.verifyStoragePermissions(ShareActivity.this);
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);
                ShareVideo video = new ShareVideo.Builder()
                        .setLocalUrl(videoFileUri)
                        .build();
                ShareVideoContent content = new ShareVideoContent.Builder()
                        .setVideo(video)
                        .build();

                shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);

//                ApplicationService.verifyStoragePermissions(ShareActivity.this);
//                Intent intent = new Intent();
//                intent.setType("video/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Complete action using"), POST);
            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApplicationService.verifyStoragePermissions(ShareActivity.this);
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);
                ShareVideo video = new ShareVideo.Builder()
                        .setLocalUrl(videoFileUri)
                        .build();
                ShareVideoContent content = new ShareVideoContent.Builder()
                        .setVideo(video)
                        .build();

                msgDialog.show(content);
            }
        });

        IGBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);

                String type = "video/*";
                // Create the new Intent using the 'Send' action.
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setPackage("com.instagram.android");


                // Set the MIME type
                share.setType(type);

                // Add the URI to the Intent.
                share.putExtra(Intent.EXTRA_STREAM, videoFileUri);

                // Broadcast the Intent.
                startActivity(Intent.createChooser(share, "Share to"));
            }
        });

        lineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);

                String type = "video/*";
                // Create the new Intent using the 'Send' action.
                Intent share = new Intent(Intent.ACTION_SEND);

                share.setPackage("jp.naver.line.android");

                // Set the MIME type
                share.setType(type);

                // Add the URI to the Intent.
                share.putExtra(Intent.EXTRA_STREAM, videoFileUri);

                // Broadcast the Intent.
                startActivity(Intent.createChooser(share, "Share to"));
            }
        });

        weiboBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);

                String type = "video/*";
                // Create the new Intent using the 'Send' action.
                Intent share = new Intent(Intent.ACTION_SEND);

                share.setPackage("com.sina.weibo");

                // Set the MIME type
                share.setType(type);

                // Add the URI to the Intent.
                share.putExtra(Intent.EXTRA_STREAM, videoFileUri);

                // Broadcast the Intent.
                startActivity(Intent.createChooser(share, "Share to"));
            }
        });

        wechatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);

                String type = "video/*";
                // Create the new Intent using the 'Send' action.
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setPackage("com.tencent.mm");

                // Set the MIME type
                share.setType(type);

                // Add the URI to the Intent.
                share.putExtra(Intent.EXTRA_STREAM, videoFileUri);

                // Broadcast the Intent.
                startActivity(Intent.createChooser(share, "Share to"));
            }
        });

        youTubeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);

                String type = "video/*";
                // Create the new Intent using the 'Send' action.
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setPackage("com.google.android.youtube");

                // Set the MIME type
                share.setType(type);

                // Add the URI to the Intent.
                share.putExtra(Intent.EXTRA_STREAM, videoFileUri);

                // Broadcast the Intent.
                startActivity(Intent.createChooser(share, "Share to"));
            }
        });

        otherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(ApplicationParameter.FILE_SAVE_PATH);
                Uri videoFileUri = Uri.fromFile(file);

                String type = "video/*";
                // Create the new Intent using the 'Send' action.
                Intent share = new Intent(Intent.ACTION_SEND);

                // Set the MIME type
                share.setType(type);

                // Add the URI to the Intent.
                share.putExtra(Intent.EXTRA_STREAM, videoFileUri);

                // Broadcast the Intent.
                startActivity(Intent.createChooser(share, "Share to"));
            }
        });



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        //below for test

//        final Uri selectedImageUri = data.getData();
//        String imagepath = getPath(this, selectedImageUri);
//        File imageFile = new File(imagepath);
//        String fileName = UUID.randomUUID().toString() + "_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".mp4";
//        new PostVideoContent(user_id, 1, videoContentTitle, fileName).execute();
//        new BackgroundUploader(this, Api.baseUrl + "upload_video_content_file.php", imageFile, fileName).execute();
    }

    class PostVideoContent extends AsyncTask<String, String, String> {
        int user_id;
        int video_sample_id;
        String title;
        String path;

        PostVideoContent(int user_id, int video_sample_id, String title, String path){
            this.user_id = user_id;
            this.video_sample_id = video_sample_id;
            this.title = title;
            this.path = path;
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
            params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
            params.add(new BasicNameValuePair("video_sample_id", String.valueOf(video_sample_id)));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("path", path));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonObject = jsonParser.makeHttpRequest(Api.baseUrl + "create_video_content.php",
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
         * **/
        protected void onPostExecute(String file_url) {
            Intent intent = new Intent(ShareActivity.this, MainActivity.class);;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

    }


    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private Intent getPackageIntent(String pkgName){
        Intent i = null;
        PackageManager manager = getPackageManager();
        try {
            i = manager.getLaunchIntentForPackage(pkgName);
            if(i!=null)
                i.addCategory(Intent.CATEGORY_LAUNCHER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return i;
    }



    private FacebookCallback<Sharer.Result> callback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            Log.e(TAG, "Succesfully posted");
            // Write some code to do some operations when you shared content successfully.
        }

        @Override
        public void onCancel() {
            Log.e(TAG, "Cancel occured");
            // Write some code to do some operations when you cancel sharing content.
        }

        @Override
        public void onError(FacebookException error) {
            Log.e(TAG, error.getMessage());
            // Write some code to do some operations when some error occurs while sharing content.
        }
    };

    FacebookCallback<Sharer.Result> messenger_callback = new FacebookCallback<Sharer.Result>(){
        @Override
        public void onSuccess(Sharer.Result result) {

        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException error) {

        }
    };

    public String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {

            if (isExternalStorageDocument(uri)) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                String storageDefinition;


                if("primary".equalsIgnoreCase(type)){

                    return Environment.getExternalStorageDirectory() + "/" + split[1];

                } else {

                    if(Environment.isExternalStorageRemovable()){
                        storageDefinition = "EXTERNAL_STORAGE";

                    } else{
                        storageDefinition = "SECONDARY_STORAGE";
                    }

                    return System.getenv(storageDefinition) + "/" + split[1];
                }

            } else if (isDownloadsDocument(uri)) {// DownloadsProvider

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);

            } else if (isMediaDocument(uri)) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore (and general)

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }

        return null;
    }

    class BackgroundUploader extends AsyncTask<Void, Integer, Void> implements DialogInterface.OnCancelListener {

        private ProgressDialog progressDialog;
        private Context context;
        private String url;
        private File file;
        private String fileName;

        public BackgroundUploader(Context context, String url, File file, String fileName) {
            this.context = context;
            this.url = url;
            this.file = file;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Uploading...");
            progressDialog.setCancelable(false);
            progressDialog.setMax((int) file.length());
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... v) {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = file.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

                int progress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[1024];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    progress += bytesRead;
                    // update progress bar
                    publishProgress(progress);
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();
                out.close();

                // Get server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder builder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }

            } catch (Exception e) {
                // Exception
            } finally {
                if (connection != null) connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress((int) (progress[0]));
        }

        @Override
        protected void onPostExecute(Void v) {
            progressDialog.dismiss();
//            Intent intent = new Intent(ShareActivity.this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }
    }
}
