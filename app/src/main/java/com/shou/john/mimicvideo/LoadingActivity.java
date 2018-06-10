package com.shou.john.mimicvideo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.john.mimicvideo.R;
import com.shou.john.mimicvideo.api.Api;
import com.shou.john.mimicvideo.utils.ApplicationParameter;
import com.shou.john.mimicvideo.utils.JSONParser;
import com.shou.john.mimicvideo.utils.SharePreferenceDB;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoadingActivity extends BaseActivity {
    NumberProgressBar numberProgressBar;
    private TextView loadingTitleTxt;
    private int to;
    private SharePreferenceDB sharePreferenceDB;
    private JSONParser jsonParser = new JSONParser();

    int allprogress = 0;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        to = getIntent().getIntExtra("to", 0);
        numberProgressBar = findViewById(R.id.number_progress_bar);
        loadingTitleTxt = findViewById(R.id.loadingTitleTxt);

        sharePreferenceDB = new SharePreferenceDB(this);
        if(getIntent().getStringExtra("video_sample_url") != null){
            int videoSampleId = getIntent().getIntExtra("videoSampleId", 0);
            String videoSampleUrl = getIntent().getStringExtra("video_sample_url");
            downLoadFileFromUrl(videoSampleUrl, ApplicationParameter.FILE_FOLDER_SAVE_PATH, ApplicationParameter.FILE_SAVE_NAME, videoSampleId);
        }else if(getIntent().getStringExtra("videoContentUrl") != null){
            int videoSampleId = 0;
            String videoContentUrl = getIntent().getStringExtra("videoContentUrl");
            downLoadFileFromUrl(videoContentUrl, ApplicationParameter.FILE_FOLDER_SAVE_PATH, ApplicationParameter.FILE_SAVE_NAME, 0);
        }else if(getIntent().getStringExtra("cameraVideoUrl") != null){
            int videoSampleId = 0;
            String cameraVideoUrl = getIntent().getStringExtra("cameraVideoUrl");
            Intent intent = new Intent();
            intent.setClass(LoadingActivity.this, VideoPreviewActivity.class);
            intent.putExtra("videoSampleId", videoSampleId);
            intent.putExtra("videoUrl", cameraVideoUrl);
            startActivity(intent);
        }else if(getIntent().getStringExtra("videoContentTitle") !=null){
            loadingTitleTxt.setText("發布中");
            int user_id = sharePreferenceDB.getInt("id");
            int videoSampleId = getIntent().getIntExtra("videoSampleId", 0);
            String videoContentTitle = getIntent().getStringExtra("videoContentTitle");
            File file = new File(ApplicationParameter.FINALLY_FILE_SAVE_PATH);
            String fileName = UUID.randomUUID().toString() + "_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".mp4";
            new PostVideoContent(user_id, videoSampleId, videoContentTitle, fileName).execute();
            new BackgroundUploader(LoadingActivity.this, Api.baseUrl + "upload_video_content_file.php", file, fileName).execute();
        }
    }

    public void downLoadFileFromUrl(String url, String dirPath, String fileName, final int videoSampleId){
        AndroidNetworking.download(url,dirPath,fileName)
                .setTag("downloadTest")
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        // do anything with progress
                        numberProgressBar.setProgress((int)((bytesDownloaded * 100) / totalBytes));
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // do anything after completion
                        Intent intent = new Intent();
                        if(to == ApplicationParameter.TO_RECORD){
                            intent.setClass(LoadingActivity.this, VideoPreviewActivity.class);
                            intent.putExtra("videoSampleId", videoSampleId);
                        }else{
                            intent.setClass(LoadingActivity.this, ShareActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        finish();
                    }
                });
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
        }

    }

    class BackgroundUploader extends AsyncTask<Void, Integer, Void> implements DialogInterface.OnCancelListener {
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

                allprogress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[1024];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    allprogress += bytesRead;
                    // update progress bar
                    publishProgress(allprogress);
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
            numberProgressBar.setProgress((int)((progress[0] * 100) / file.length()));
        }

        @Override
        protected void onPostExecute(Void v) {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }
    }
}
