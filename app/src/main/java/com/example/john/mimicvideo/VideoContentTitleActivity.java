package com.example.john.mimicvideo;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.john.mimicvideo.utils.ApplicationParameter;
import com.example.john.mimicvideo.utils.ApplicationService;
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

public class VideoContentTitleActivity extends BaseActivity {
    private String TAG = VideoContentTitleActivity.class.getSimpleName();
    private SimpleExoPlayerView videoContentPlayerView;
    private ImageView saveVideoContentImg;
    private EditText videoContentEdit;
    private ImageView toShareImg;
    private TextView backTxt;
    private int videoSampleId;

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
        if(videoContentPlayerView.getPlayer() != null) {
            videoContentPlayerView.getPlayer().release();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_content_title);
        videoContentPlayerView = findViewById(R.id.videoContentPlayerView);
        saveVideoContentImg = findViewById(R.id.saveVideoContentImg);
        videoContentEdit = findViewById(R.id.videoContentEdit);
        toShareImg = findViewById(R.id.toShareImg);
        backTxt = findViewById(R.id.backTxt);

        videoSampleId = getIntent().getIntExtra("videoSampleId", 0);

        showVideo();


        saveVideoContentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        toShareImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("videoContentTitle", videoContentEdit.getText().toString());
                intent.putExtra("videoSampleId", videoSampleId);
                intent.setClass(VideoContentTitleActivity.this, ShareActivity.class);
                startActivity(intent);
            }
        });

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(R.string.fa_angle_left);
        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final GestureDetector gestureDetector = new
                GestureDetector(VideoContentTitleActivity.this,
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
        player = ExoPlayerFactory.newSimpleInstance(VideoContentTitleActivity.this,trackSelector,loadControl);
        // 将player关联到View上
        videoContentPlayerView.setPlayer(player);
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(VideoContentTitleActivity.this,
                Util.getUserAgent(VideoContentTitleActivity.this, "yourApplicationName"), bandwidthMeter2);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource;
        // test hls
        //url="http://hls.videocc.net/ce0812b122/c/ce0812b122c492470605bc47d3388a09_3.m3u8";
        if(ApplicationParameter.FILE_SAVE_PATH.contains(".m3u8")){
//            videoSource =new HlsMediaSource(Uri.parse(url),dataSourceFactory,null,null);
            videoSource = new ExtractorMediaSource(Uri.parse(ApplicationParameter.FILE_SAVE_PATH),
                    dataSourceFactory, extractorsFactory, null, null);
        }else{
            //test mp4
            videoSource = new ExtractorMediaSource(Uri.parse(ApplicationParameter.FILE_SAVE_PATH),
                    dataSourceFactory, extractorsFactory, null, null);
        }

        player.prepare(videoSource);
        player.setRepeatMode(1);
        player.setPlayWhenReady(true);
    }
}
