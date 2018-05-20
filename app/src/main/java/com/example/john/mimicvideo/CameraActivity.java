package com.example.john.mimicvideo;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.john.mimicvideo.model.RecordAudio;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.MediaUtil;
import com.example.john.mimicvideo.view.VideoTrimmer.K4LVideoTrimmer;
import com.example.john.mimicvideo.view.VideoTrimmer.interfaces.OnRangeSeekBarListener;
import com.example.john.mimicvideo.view.VideoTrimmer.view.ProgressBarView;
import com.example.john.mimicvideo.view.VideoTrimmer.view.RangeSeekBarView;
import com.example.john.mimicvideo.view.VideoTrimmer.view.Thumb;
import com.example.john.mimicvideo.view.VideoTrimmer.view.TimeLineView;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;

import com.example.john.mimicvideo.adapter.ReportDescriptionAdapter;
import com.example.john.mimicvideo.adapter.VideoFrameAdapter;
import com.example.john.mimicvideo.utils.JSONParser;
import com.google.android.exoplayer2.ExoPlayerFactory;
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
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.example.john.mimicvideo.utils.FilePath.getDataColumn;
import static com.example.john.mimicvideo.utils.FilePath.isDownloadsDocument;
import static com.example.john.mimicvideo.utils.FilePath.isExternalStorageDocument;
import static com.example.john.mimicvideo.utils.FilePath.isGooglePhotosUri;
import static com.example.john.mimicvideo.utils.FilePath.isMediaDocument;

public class CameraActivity extends BaseActivity {
    private String TAG = CameraActivity.class.getSimpleName();
    private ImageView reportImg;
    private ImageView startRecordImg;
    private SimpleExoPlayerView videoContentPlayerView;
    private SimpleExoPlayer player ;
    private JSONParser jsonParser = new JSONParser();
    private FFmpeg ffmpeg;

    Handler handler;
    TextView tv;
    MediaRecorder mRecorder;
    String fileName;
    Boolean isRecording;
    int recordTime,playTime;
    SeekBar seekBar;
    MediaPlayer mPlayer;

    private int video_sample_id;
    private String video_sample_url;


    //below for video handle
    private int mDuration = 0;
    private int mTimeVideo = 0;
    private int mStartPosition = 0;
    private int mEndPosition = 0;
    private int mMaxDuration = 40;
    private Uri mSrc;
    private TimeLineView timeLineView;
    private RangeSeekBarView mRangeSeekBarView;
    private VideoView mVideoView;
    private ProgressBarView mVideoProgressIndicator;

    private Button muteBtn;

    private TextView toPostTxt;

    private ImageView videoPlayIconImg;

    private Button deleteFirstBtn;
    private Button deleteSecondBtn;
    private Button deleteThirdBtn;

    private TextView cutVideoTxt;

    private String videoContentPath;

    List<RecordAudio>recordAudioList = new ArrayList<>();
    SoundPool soundPool ;
    private boolean is_play = false;
    private boolean is_cut = false;


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
        setContentView(R.layout.activity_camera);
        reportImg = findViewById(R.id.reportImg);
        startRecordImg = findViewById(R.id.startRecordImg);
        videoContentPlayerView = findViewById(R.id.videoContentPlayerView);
        videoContentPlayerView.setControllerVisibilityListener(null);;
        tv=findViewById(R.id.txttime);
        seekBar=findViewById(R.id.seek1);

        muteBtn = findViewById(R.id.muteBtn);

        toPostTxt = findViewById(R.id.toPostTxt);

        videoPlayIconImg = findViewById(R.id.videoPlayIconImg);

        timeLineView = ((TimeLineView) findViewById(R.id.timeLineView));
        mRangeSeekBarView = ((RangeSeekBarView) findViewById(R.id.timeLineBar));

        deleteFirstBtn = findViewById(R.id.deleteFirstAudioBtn);
        deleteSecondBtn = findViewById(R.id.deleteSecondAudioBtn);
        deleteThirdBtn = findViewById(R.id.deleteThirdAufdioBtn);

        cutVideoTxt = findViewById(R.id.cutVideoTxt);

        mVideoView = findViewById(R.id.video_loader);

        cutVideoTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_cut){
                    mRangeSeekBarView.setVisibility(View.GONE);
                    is_cut = false;
                }else{
                    mRangeSeekBarView.setVisibility(View.VISIBLE);
                    is_cut = true;
                }
            }
        });

        video_sample_id = getIntent().getIntExtra("video_sample_id", 0);
        video_sample_url = getIntent().getStringExtra("video_sample_url");

        handler=new Handler();
        fileName=Environment.getExternalStorageDirectory()+ "/audio"+System.currentTimeMillis()+".mp3";
        isRecording=false;

//        setUpListeners();
//        loadFFMpegBinary();
        showVideoTest(Uri.parse(Environment.getExternalStorageDirectory()+ "/cut_video1" +".mp4"));
        //showVideo();
        onVideoPrepared();

        soundPool = new SoundPool(100, 0,10);

        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMixAudio();
            }
        });



//        videoContentPlayerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {//当前状态
//                    case MotionEvent.ACTION_DOWN:
//                        if(videoContentPlayerView.getPlayer().getPlayWhenReady()){
//                            pauseVideoTxt.setVisibility(View.VISIBLE);
//                            videoContentPlayerView.getPlayer().setPlayWhenReady(false);
//                        }else{
//                            pauseVideoTxt.setVisibility(View.GONE);
//                            videoContentPlayerView.getPlayer().setPlayWhenReady(true);
//                        }
//                        break;
//                }
//                return false;
//            }
//        });

        reportImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(CameraActivity.this, R.style.selectorDialog);
                dialog.setContentView(R.layout.dialog_report);
                RecyclerView reportDescriptionRV = dialog.findViewById(R.id.reportDescriptionRV);
                Button reportSubmitBtn = dialog.findViewById(R.id.reportSubmitBtn);
                reportSubmitBtn.setEnabled(false);

                LinearLayoutManager layoutManager = new LinearLayoutManager(CameraActivity.this, LinearLayoutManager.VERTICAL, false);
                final ReportDescriptionAdapter reportDescriptionAdapter = new ReportDescriptionAdapter(CameraActivity.this, reportSubmitBtn);
                reportDescriptionRV.setLayoutManager(layoutManager);
                reportDescriptionRV.setAdapter(reportDescriptionAdapter);

                reportSubmitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new ReportSubmit("").execute();
                    }
                });


                // 由程式設定 Dialog 視窗外的明暗程度, 亮度從 0f 到 1f
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount = 0.5f;
                dialog.getWindow().setAttributes(lp);
                dialog.show();
            }
        });

        startRecordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ApplicationService.verifyStoragePermissions(CameraActivity.this);
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), 3);
            }
        });

        toPostTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(CameraActivity.this, VideoContentTitleActivity.class);
                intent.putExtra("videoContentPath", videoContentPath);
                startActivity(intent);
            }
        });

        deleteFirstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.unload(recordAudioList.get(0).id);
                recordAudioList.remove(0);
                if(recordAudioList.size() == 2){
                    deleteThirdBtn.setVisibility(View.GONE);
                }else if(recordAudioList.size() == 1){
                    deleteThirdBtn.setVisibility(View.GONE);
                    deleteSecondBtn.setVisibility(View.GONE);
                }else{
                    deleteThirdBtn.setVisibility(View.GONE);
                    deleteSecondBtn.setVisibility(View.GONE);
                    deleteFirstBtn.setVisibility(View.GONE);
                }
            }
        });

        deleteSecondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.unload(recordAudioList.get(1).id);
                recordAudioList.remove(1);
                if(recordAudioList.size() == 2){
                    deleteThirdBtn.setVisibility(View.GONE);
                }else if(recordAudioList.size() == 1){
                    deleteThirdBtn.setVisibility(View.GONE);
                    deleteSecondBtn.setVisibility(View.GONE);
                }
            }
        });

        deleteThirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.unload(recordAudioList.get(2).id);
                recordAudioList.remove(2);
                if(recordAudioList.size() == 2){
                    deleteThirdBtn.setVisibility(View.GONE);
                }
            }
        });

//        int videoFrameCount = (int)(timeInMillisec / 1000) / 5;

//        mediaMetadataRetriever.setDataSource(uriPath, new HashMap<String, String>());
//        for(int i = 0; i < videoFrameCount; i++){
//            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(i * 1000000 * 5); //unit in microsecond
//            videoFrameList.add(bmFrame);
//        }

//        MediaUtil.DownloadFromUrl(video_sample_url, "/storage/emulated/0/gg.mp4");
        //new MediaUtil.DownloadFileFromURL(video_sample_url, "").execute();
    }

    public void setUpListeners(){
        final GestureDetector gestureDetector = new
                GestureDetector(CameraActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        onClickVideoPlayPause();
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


        mRangeSeekBarView.addOnRangeSeekBarListener(new OnRangeSeekBarListener() {
            @Override
            public void onCreate(RangeSeekBarView rangeSeekBarView, int index, float value) {
                // Do nothing
            }

            @Override
            public void onSeek(RangeSeekBarView rangeSeekBarView, int index, float value) {
                onSeekThumbs(index, value);
            }

            @Override
            public void onSeekStart(RangeSeekBarView rangeSeekBarView, int index, float value) {
                // Do nothing
            }

            @Override
            public void onSeekStop(RangeSeekBarView rangeSeekBarView, int index, float value) {
                onStopSeekThumbs();
            }
        });
        //mRangeSeekBarView.addOnRangeSeekBarListener(mVideoProgressIndicator);
    }

    private void onSeekThumbs(int index, float value) {
        switch (index) {
            case Thumb.LEFT: {
                mStartPosition = (int) ((mDuration * value) / 100L);
                //mVideoView.seekTo(mStartPosition);
                videoContentPlayerView.getPlayer().seekTo(mStartPosition);
                break;
            }
            case Thumb.RIGHT: {
                mEndPosition = (int) ((mDuration * value) / 100L);
                break;
            }
        }
        setProgressBarPosition(mStartPosition);

        //setTimeFrames();
        mTimeVideo = mEndPosition - mStartPosition;
    }

    private void onStopSeekThumbs() {
        //mMessageHandler.removeMessages(SHOW_PROGRESS);
       // mVideoView.pause();
        videoContentPlayerView.getPlayer().setPlayWhenReady(false);
        //mPlayView.setVisibility(View.VISIBLE);
        videoPlayIconImg.setVisibility(View.VISIBLE);
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
        player = ExoPlayerFactory.newSimpleInstance(CameraActivity.this,trackSelector,loadControl);
        // 将player关联到View上
        videoContentPlayerView.setPlayer(player);
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(CameraActivity.this,
                Util.getUserAgent(CameraActivity.this, "yourApplicationName"), bandwidthMeter2);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource;
        // test hls
        //url="http://hls.videocc.net/ce0812b122/c/ce0812b122c492470605bc47d3388a09_3.m3u8";
        if(video_sample_url.contains(".m3u8")){
//            videoSource =new HlsMediaSource(Uri.parse(url),dataSourceFactory,null,null);
            videoSource = new ExtractorMediaSource(Uri.parse(video_sample_url),
                    dataSourceFactory, extractorsFactory, null, null);
        }else{
            //test mp4
            videoSource = new ExtractorMediaSource(Uri.parse(video_sample_url),
                    dataSourceFactory, extractorsFactory, null, null);
        }

        player.prepare(videoSource);

        mSrc = Uri.parse(video_sample_url);
        timeLineView.setVideo(mSrc);
    }

    public void showVideoTest(Uri uri){
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
        player = ExoPlayerFactory.newSimpleInstance(CameraActivity.this,trackSelector,loadControl);
        // 将player关联到View上
        videoContentPlayerView.setPlayer(player);
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(CameraActivity.this,
                Util.getUserAgent(CameraActivity.this, "yourApplicationName"), bandwidthMeter2);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource;
        // test hls
        //url="http://hls.videocc.net/ce0812b122/c/ce0812b122c492470605bc47d3388a09_3.m3u8";
        if(video_sample_url.contains(".m3u8")){
//            videoSource =new HlsMediaSource(Uri.parse(url),dataSourceFactory,null,null);
            videoSource = new ExtractorMediaSource(uri,
                    dataSourceFactory, extractorsFactory, null, null);
        }else{
            //test mp4
            videoSource = new ExtractorMediaSource(uri,
                    dataSourceFactory, extractorsFactory, null, null);
        }

        player.prepare(videoSource);

        mSrc = Uri.parse("/storage/emulated/0/cut_video1.mp4");
        timeLineView.setVideo(mSrc);
    }



    private void onClickVideoPlayPause() {
        if (videoContentPlayerView.getPlayer().getPlayWhenReady()) {
            videoPlayIconImg.setVisibility(View.VISIBLE);
//            mMessageHandler.removeMessages(SHOW_PROGRESS);
            videoContentPlayerView.getPlayer().setPlayWhenReady(false);
        } else {
            videoPlayIconImg.setVisibility(View.GONE);
//
//            if (mResetSeekBar) {
//                mResetSeekBar = false;
//                mVideoView.seekTo(mStartPosition);
//            }
//
//            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
            videoContentPlayerView.getPlayer().setPlayWhenReady(true);
        }
    }

    public void DownloadFromUrl(String imageURL, String fileName) {  //this is the downloader method
        try {
            URL url = new URL(imageURL); //you can write here any link
            File file = new File(fileName);

            long startTime = System.currentTimeMillis();
            Log.d("ImageManager", "download begining");
            Log.d("ImageManager", "download url:" + url);
            Log.d("ImageManager", "downloaded file name:" + fileName);
                        /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();

                        /*
                         * Define InputStreams to read from the URLConnection.
                         */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

                        /*
                         * Read bytes to the Buffer until there is nothing more to read(-1).
                         */
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

                        /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.close();
            Log.d("ImageManager", "download ready in"
                    + ((System.currentTimeMillis() - startTime) / 1000)
                    + " sec");

        } catch (IOException e) {
            Log.d("ImageManager", "Error: " + e);
        }

    }


    // UPDATED!
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


    /**
     * Command for cutting video
     */
    private void executeCutVideoCommand(int startMs, int endMs, Uri selectedVideoUri) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );

        String filePrefix = "cut_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(CameraActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);
        String filePath = dest.getAbsolutePath();
//        String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};

        execFFmpegBinary(complexCommand);

    }


    //comment for add audio to specific video position
    private void executeAddAudioToVideoCommand(Uri selectedVideoUri){
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "cut_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(CameraActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
//        videoContentPath = dest.getAbsolutePath();
        videoContentPath = "/storage/emulated/0/Movies/cut_video1.mp4";
        switch(recordAudioList.size()){
            case 0:{
                Toast.makeText(this, "尚未錄製聲音", Toast.LENGTH_SHORT).show();
                break;
            }
            case 1:{
                String[] complexCommand  = {"-i", yourRealPath, "-i", recordAudioList.get(0).path, "-filter_complex", "[1]adelay=1000|1000[aud];[0][aud]amix", "-c:v", "copy", videoContentPath};
                execFFmpegBinary(complexCommand);
                break;
            }
            case 2:{
                String[] complexCommand = {"-i", yourRealPath, "-i", recordAudioList.get(0).path, "-i", recordAudioList.get(1).path, "-filter_complex",
                "[1]adelay=1000|1000[aud1];[2]adelay=2000|2000[aud2];[0][aud1][aud2]amix=3", "-c:v", "copy", videoContentPath};
                execFFmpegBinary(complexCommand);
                break;
            }
            case 3:{
                String[] complexCommand = {"-i", yourRealPath, "-i", recordAudioList.get(0).path, "-i", recordAudioList.get(1).path, "-i", recordAudioList.get(2).path, "-filter_complex",
                "[1]adelay=1000|1000[aud1];[2]adelay=2000|2000[aud2];[3]adelay=5000|5000[aud3];[0][aud1][aud2][aud3]amix=4", "-c:v", "copy", videoContentPath};
                execFFmpegBinary(complexCommand);
                break;
            }
        }
//        String[] complexCommand = {"-i", yourRealPath, "-i", audioList.get(0), "-filter_complex", "[1]adelay=10000|10000[aud];[0][aud]amix", "-c:v", "copy", filePath};
//        ffmpeg -i in.avi -i audio1.wav -i audio2.wav -filter_complex
//        "[1]adelay=30000|30000[aud1];[2]adelay=90000|90000[aud2];
//                [0][aud1][aud2]amix=3" -c:v copy out.avi

//        String[] complexCommand = {"-i", yourRealPath, "-i", audioList.get(0), "-i", audioList.get(1), "-filter_complex",
//                "[1]adelay=10000|10000[aud1];[2]adelay=6000|6000[aud2];[0][aud1][aud2]amix=3", "-c:v", "copy", videoContentPath};
//        String[] complexCommand = {"-i", yourRealPath, "-i", audioList.get(0), "-i", audioList.get(1), "-i", audioList.get(2), "-filter_complex",
//                "[1]adelay=10000|10000[aud1];[2]adelay=6000|6000[aud2];[3]adelay=7000|7000[aud3];[0][aud1][aud2][aud3]amix=4", "-c:v", "copy", videoContentPath};
    }

    //comment for mute video
    public void executeMuteVideoCommand(Uri selectedVideoUri){
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "cut_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(CameraActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
        String filePath = dest.getAbsolutePath();

        String[] complexCommand = {"-i", yourRealPath, "-an", filePath};

        execFFmpegBinary(complexCommand);
    }

    /**
     * Executing ffmpeg binary
     */
    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);
//                    if (choice == 1 || choice == 2 || choice == 5 || choice == 6 || choice == 7) {
//                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                    } else if (choice == 3) {
//                        Intent intent = new Intent(MainActivity.this, PreviewImageActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                    } else if (choice == 4) {
//                        Intent intent = new Intent(MainActivity.this, AudioPreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                    } else if (choice == 8) {
//                        choice = 9;
//                        reverseVideoCommand();
//                    } else if (Arrays.equals(command, lastReverseCommand)) {
//                        choice = 10;
//                        concatVideoCommand();
//                    } else if (choice == 10) {
//                        File moviesDir = Environment.getExternalStoragePublicDirectory(
//                                Environment.DIRECTORY_MOVIES
//                        );
//                        File destDir = new File(moviesDir, ".VideoPartsReverse");
//                        File dir = new File(moviesDir, ".VideoSplit");
//                        if (dir.exists())
//                            deleteDir(dir);
//                        if (destDir.exists())
//                            deleteDir(destDir);
//                        choice = 11;
//                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started progress : ffmpeg " + s);
//                    if (choice == 8)
//                        progressDialog.setMessage("progress : splitting video " + s);
//                    else if (choice == 9)
//                        progressDialog.setMessage("progress : reversing splitted videos " + s);
//                    else if (choice == 10)
//                        progressDialog.setMessage("progress : concatenating reversed videos " + s);
//                    else
//                        progressDialog.setMessage("progress : " + s);
//                    Log.d(TAG, "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
//                    progressDialog.setMessage("Processing...");
//                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
//                    if (choice != 8 && choice != 9 && choice != 10) {
//                        progressDialog.dismiss();
//                    }

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    /**
     * Load FFmpeg binary
     */
    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : era nulo");
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.d(TAG, "EXception no controlada : " + e);
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(CameraActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CameraActivity.this.finish();
                    }
                })
                .create()
                .show();

    }


    public void startRecording(View view){
        if(recordAudioList.size() < 3){
            if(!isRecording){
                //Create MediaRecorder and initialize audio source, output format, and audio encoder
                mRecorder = new MediaRecorder();
                mRecorder.reset();
                mRecorder.setAudioSource( MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP);

                if(recordAudioList.size() == 0){
//                    fileName=Environment.getExternalStorageDirectory()+ "/audio"+System.currentTimeMillis()+".mp3";
                    fileName=Environment.getExternalStorageDirectory()+ "/audio1" +".mp3";
                }else if(recordAudioList.size() == 1){
                    fileName=Environment.getExternalStorageDirectory()+ "/audio2" +".mp3";
                }else if(recordAudioList.size() == 2){
                    fileName=Environment.getExternalStorageDirectory()+ "/audio3" +".mp3";
                }
                mRecorder.setOutputFile(fileName);
                mRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB);
                // Starting record time
                recordTime=0;
                // Show TextView that displays record time
                tv.setVisibility(TextView.VISIBLE);
                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e("LOG_TAG", "prepare failed");
                }
                // Start record job
                mRecorder.start();
                // Change isRecroding flag to true
                isRecording=true;
                // Post the record progress
                handler.post(UpdateRecordTime);
                is_play = false;
            }
        }else{
            Toast.makeText(this, "超過數量 " + recordAudioList.get(0) + "\n" + recordAudioList.get(1) + "\n" + recordAudioList.get(2), Toast.LENGTH_SHORT).show();
        }
    }
    public void stopRecording(View view){
        if(isRecording){
            // Stop recording and release resource
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            // Change isRecording flag to false
            isRecording=false;
            // Hide TextView that shows record time
            tv.setVisibility(TextView.GONE);
            playIt(); // Play the audio

            RecordAudio recordAudio = new RecordAudio();
            recordAudio.id = soundPool.load(fileName,0);
            recordAudio.path = fileName;
            recordAudio.startTime = 1;
            recordAudio.endTime = 5;

            recordAudioList.add(recordAudio);
            if(recordAudioList.size() == 1){
                deleteFirstBtn.setVisibility(View.VISIBLE);
            }else if(recordAudioList.size() == 2){
                deleteSecondBtn.setVisibility(View.VISIBLE);
            }else if(recordAudioList.size() == 3){
                deleteThirdBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    public void playIt(){
        // Create MediaPlayer object
        mPlayer = new MediaPlayer();
        // set start time
        playTime=0;
        // Reset max and progress of the SeekBar
        seekBar.setMax(recordTime);
        seekBar.setProgress(0);
        try {
            // Initialize the player and start playing the audio
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
            // Post the play progress
            handler.post(UpdatePlayTime);
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare failed");
        }
    }

    public void playMixAudio(){
        if(recordAudioList.size() == 0){
            Toast.makeText(CameraActivity.this, "尚未錄製聲音，請錄製", Toast.LENGTH_SHORT).show();
        }else{
            recordTime = 0;
            is_play = true;
            handler.post(TimeForAudio);
        }
    }

    Runnable TimeForAudio = new Runnable() {
        @Override
        public void run() {
            if(is_play){
                tv.setText(String.valueOf(recordTime));
                recordTime+=1;
                // Delay 1s before next call
                if(recordAudioList.size() == 1){
                    if(recordTime == recordAudioList.get(0).startTime){
                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                    }
                }else if(recordAudioList.size() == 2){
                    if(recordTime == recordAudioList.get(0).startTime){
                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                    }
                    if(recordTime == recordAudioList.get(1).startTime){
                        soundPool .play(recordAudioList.get(1).id,1,1, 0, 0, 1);
                    }
                }else if(recordAudioList.size() == 3){
                    if(recordTime == recordAudioList.get(0).startTime){
                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                    }
                    if(recordTime == recordAudioList.get(1).startTime){
                        soundPool .play(recordAudioList.get(1).id,1,1, 0, 0, 1);
                    }
                    if(recordTime == recordAudioList.get(2).startTime){
                        soundPool .play(recordAudioList.get(2).id,1,1, 0, 0, 1);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    Runnable UpdateRecordTime=new Runnable(){
        public void run(){
            if(isRecording){
                tv.setText(String.valueOf(recordTime));
                recordTime+=1;
                // Delay 1s before next call
                handler.postDelayed(this, 1000);
            }
        }
    };
    Runnable UpdatePlayTime=new Runnable(){
        public void run(){
            if(mPlayer.isPlaying()){
                tv.setText(String.valueOf(playTime));
                // Update play time and SeekBar
                playTime+=1;
                seekBar.setProgress(playTime);
                // Delay 1s before next call
                handler.postDelayed(this, 1000);
            }
        }
    };



    private void onVideoPrepared() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(Environment.getExternalStorageDirectory()+ "/cut_video" +".mp4", new HashMap<String, String>());
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mDuration = Integer.parseInt(time);
        setSeekBarPosition();
    }

    private void setSeekBarPosition() {

        if (mDuration >= mMaxDuration) {
            mStartPosition = mDuration / 2 - mMaxDuration / 2;
            mEndPosition = mDuration / 2 + mMaxDuration / 2;

            mRangeSeekBarView.setThumbValue(0, (mStartPosition * 100) / mDuration);
            mRangeSeekBarView.setThumbValue(1, (mEndPosition * 100) / mDuration);

        } else {
            mStartPosition = 0;
            mEndPosition = mDuration;
        }

        setProgressBarPosition(mStartPosition);
        videoContentPlayerView.getPlayer().seekTo(mStartPosition);

        mTimeVideo = mDuration;
        mRangeSeekBarView.initMaxWidth();
    }

    private void setProgressBarPosition(int position) {
        if (mDuration > 0) {
            long pos = 1000L * position / mDuration;
        }
    }

    public void setMaxDuration(int maxDuration) {
        mMaxDuration = maxDuration * 1000;
    }

    //send the report description
    class ReportSubmit extends AsyncTask<String, String, String> {
        String reportDescription;

        ReportSubmit(String reportDescription) {
            this.reportDescription = reportDescription;
        }

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {
//            String name = inputName.getText().toString();
//            String price = inputPrice.getText().toString();
//            String description = inputDesc.getText().toString();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("type", "0"));
            params.add(new BasicNameValuePair("reportDescription", reportDescription));


            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonObject = jsonParser.makeHttpRequest("http://1.34.63.239/create_report.php",
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
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 3){
            Uri selectedImageUri = data.getData();
//            executeCutVideoCommand(3000, 4000, selectedImageUri);
            executeAddAudioToVideoCommand(selectedImageUri);
//            executeMuteVideoCommand(selectedImageUri);

//            String yourRealPath = getPath(CameraActivity.this, selectedImageUri);

//            showVideoTest(selectedImageUri);
//            onVideoPrepared();
        }
    }

}
