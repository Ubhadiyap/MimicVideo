package com.example.john.mimicvideo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.john.mimicvideo.utils.ApplicationParameter;

public class LoadingActivity extends BaseActivity {
    NumberProgressBar numberProgressBar;
    private int to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        to = getIntent().getIntExtra("to", 0);
        numberProgressBar = findViewById(R.id.number_progress_bar);
        if(getIntent().getIntExtra("video_sample_id", 0) != 0){
            int videoSampleId = getIntent().getIntExtra("video_sample_id", 0);
            String videoSampleUrl = getIntent().getStringExtra("video_sample_url");
            downLoadFileFromUrl(videoSampleUrl, "/storage/emulated/0/", "share_content.mp4", videoSampleId);
        }else if(getIntent().getStringExtra("videoContentUrl") != null){
            int videoSampleId = 0;
            String videoContentUrl = getIntent().getStringExtra("videoContentUrl");
            downLoadFileFromUrl(videoContentUrl, "/storage/emulated/0/", "share_content.mp4", 0);
        }else if(getIntent().getStringExtra("cameraVideoUrl") != null){
            int videoSampleId = 0;
            String cameraVideoUrl = getIntent().getStringExtra("cameraVideoUrl");
            Intent intent = new Intent();
            intent.setClass(LoadingActivity.this, TestVideoActivity.class);
            intent.putExtra("videoSampleId", videoSampleId);
            intent.putExtra("videoUrl", cameraVideoUrl);
            startActivity(intent);
        }
    }

    public void downLoadFileFromUrl(String url, String dirPath, String fileName, int videoSampleId){
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
                            intent.setClass(LoadingActivity.this, TestVideoActivity.class);
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

    @Override
    public void onBackPressed() {
    }
}
