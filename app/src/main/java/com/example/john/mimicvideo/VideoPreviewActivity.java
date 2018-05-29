package com.example.john.mimicvideo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.androidnetworking.AndroidNetworking;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.john.mimicvideo.model.RecordAudio;
import com.example.john.mimicvideo.utils.ApplicationParameter;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.view.DrawView;
import com.example.john.mimicvideo.view.VideoTrimmer.interfaces.OnK4LVideoListener;
import com.example.john.mimicvideo.view.VideoTrimmer.interfaces.OnProgressVideoListener;
import com.example.john.mimicvideo.view.VideoTrimmer.interfaces.OnRangeSeekBarListener;
import com.example.john.mimicvideo.view.VideoTrimmer.interfaces.OnTrimVideoListener;
import com.example.john.mimicvideo.view.VideoTrimmer.view.ProgressBarView;
import com.example.john.mimicvideo.view.VideoTrimmer.view.RangeSeekBarView;
import com.example.john.mimicvideo.view.VideoTrimmer.view.Thumb;
import com.example.john.mimicvideo.view.VideoTrimmer.view.TimeLineView;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.jacksonandroidnetworking.JacksonParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static com.example.john.mimicvideo.view.VideoTrimmer.utils.TrimVideoUtils.stringForTime;

public class VideoPreviewActivity extends BaseActivity implements OnTrimVideoListener, OnK4LVideoListener{

    private static final String TAG = VideoPreviewActivity.class.getSimpleName();
    private static final int MIN_TIME_FRAME = 1000;
    private static final int SHOW_PROGRESS = 2;

//    private SeekBar mHolderTopView;
//    private RangeSeekBarView mRangeSeekBarView;
//    private RelativeLayout mLinearVideo;
//    private View mTimeInfoContainer;
//    private VideoView mVideoView;
//    private ImageView mPlayView;
//    private TextView mTextSize;
//    private TextView mTextTimeFrame;
//    private TextView mTextTime;
//    private TimeLineView mTimeLineView;
//    private ImageView muteVideoImg;
//    private Button recordBtn;
//    private ImageView cutVideoImg;
//    private Button deleteFirstBtn;
//    private Button deleteSecondBtn;
//    private Button deleteThirdBtn;
//    private ImageView nextImg;
//    private RelativeLayout recordLineLayout1;
//    private RelativeLayout recordLineLayout2;
//    private RelativeLayout recordLineLayout3;
//    private TextView deleteLineTxt1;
//    private TextView deleteLineTxt2;
//    private TextView deleteLineTxt3;
//    private LinearLayout linearLayout1;
//    private LinearLayout linearLayout2;
//    private LinearLayout linearLayout3;
//    private DrawView drawView1;
//    private DrawView drawView2;
//    private DrawView drawView3;
//
//    private ProgressBarView mVideoProgressIndicator;
//    private Uri mSrc;
//    private String mFinalPath;
//
//    private int mMaxDuration;
//    private List<OnProgressVideoListener> mListeners;
//
//    private OnTrimVideoListener mOnTrimVideoListener;
//    private OnK4LVideoListener mOnK4LVideoListener;
//
//    private int mDuration = 0;
//    private int mTimeVideo = 0;
//    private int mStartPosition = 0;
//    private int mEndPosition = 0;
//    private int currentDuration;
//    private boolean isMute = false;
//    private boolean isCut = true;
//
//    private long mOriginSizeFile;
//    private boolean mResetSeekBar = true;
//    private final MessageHandler mMessageHandler = new MessageHandler();
//
//    private ProgressDialog mProgressDialog;
//
//    //below record audio
//    List<RecordAudio>recordAudioList = new ArrayList<>();
//    private boolean isRecording = false;
//    MediaRecorder mRecorder;
//    private String fileName;
//    private SoundPool soundPool ;
//    private int recordTime = 0;
//    private Handler handler;
//    private int limitRecordTime = 0;
//    private int startRecordTime = 0;
//    private int endRecordTime = 0;
//
//    //below handle video
//    private FFmpeg ffmpeg;
//    private String originalFileName;
//    private String handleFileName;
//
//    //below mute video
//    private MediaPlayer mediaPlayer = new MediaPlayer();
//
//    int videoSampleId = 0;

    private SeekBar mHolderTopView;
    private RangeSeekBarView mRangeSeekBarView;
    private RelativeLayout mLinearVideo;
    private View mTimeInfoContainer;
    private VideoView mVideoView;
    private ImageView mPlayView;
    private TextView mTextSize;
    private TextView mTextTimeFrame;
    private TextView mTextTime;
    private TimeLineView mTimeLineView;

    private ImageView muteVideoImg;
    private ImageView nextImg;
    private ImageView recordImg;

    //below for draw record line
    private RelativeLayout recordLineLayout1;
    private RelativeLayout recordLineLayout2;
    private RelativeLayout recordLineLayout3;
    private TextView deleteLineTxt1;
    private TextView deleteLineTxt2;
    private TextView deleteLineTxt3;
    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;
    private DrawView drawView1;
    private DrawView drawView2;
    private DrawView drawView3;
    private TextView backTxt;

    private ProgressBarView mVideoProgressIndicator;
    private Uri mSrc;
    private String mFinalPath;

    private int mMaxDuration;
    private List<OnProgressVideoListener> mListeners;

    private OnTrimVideoListener mOnTrimVideoListener;
    private OnK4LVideoListener mOnK4LVideoListener;

    private int mDuration = 0;
    private int mTimeVideo = 0;
    private int mStartPosition = 0;
    private int mEndPosition = 0;

    private long mOriginSizeFile;
    private boolean mResetSeekBar = false;
    private final MessageHandler mMessageHandler = new MessageHandler();

    private ProgressDialog mProgressDialog;


    //以下錄音用
    List<RecordAudio>recordAudioList = new ArrayList<>();
    private boolean isRecording = false;
    MediaRecorder mRecorder;
    private String fileName;
    private SoundPool soundPool = new SoundPool(100, AudioManager.STREAM_MUSIC,10);
    private int recordTime = 0;
    private Handler handler;
    private int limitRecordTime = 0;
    private int startRecordTime = 0;
    private int endRecordTime = 0;
    private int currentDuration;
    private int startRecordCoordinate;

    //以下處理影片使用
    private FFmpeg ffmpeg;
    private String originalFileName;
    private String handleFileName;

    //以下靜音用
    private boolean isMute = false;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    //以下剪影片用
    private boolean isCut = false;

    //以下播放錄音用
    private boolean firstAudioPlay = false;
    private boolean secondAudioPlay = false;
    private boolean thirdAudioPlay = false;

    int videoSampleId = 0;

    //以下為progress bar
    private Dialog loadDialog;



    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
//        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.setMessage("合成中");
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        videoSampleId = getIntent().getIntExtra("videoSampleId", 0);

//        if(getIntent().getIntExtra("video_sample_id", 0) != 0){
//            videoSampleId = getIntent().getIntExtra("video_sample_id", 0);
//            String videoSampleUrl = getIntent().getStringExtra("video_sample_url");
//            downLoadFileFromUrl(videoSampleUrl, "/storage/emulated/0/", "share_content.mp4");
//        }else if(getIntent().getStringExtra("videoContentUrl") != null){
//            String videoContentUrl = getIntent().getStringExtra("videoContentUrl");
//            new DownloadFileFromURL(videoContentUrl).execute();
//        }else if(getIntent().getStringExtra("cameraVideoUrl") != null){
//            String cameraVideoUrl = getIntent().getStringExtra("cameraVideoUrl");
////            setVideoURI(Uri.parse( "/storage/emulated/0/share_content.mp4"));
//            init();
//            setVideoURI(Uri.parse(cameraVideoUrl));
//            setMaxDuration(40);
//            setOnTrimVideoListener(this);
//            setOnK4LVideoListener(this);
//        }

        init();
        setVideoURI(Uri.parse(ApplicationParameter.FILE_SAVE_PATH));
        setMaxDuration(40);
        setOnTrimVideoListener(VideoPreviewActivity.this);
        setOnK4LVideoListener(VideoPreviewActivity.this);
    }

    @Override
    public void onTrimStarted() {
        mProgressDialog.show();
    }

    @Override
    public void getResult(final Uri uri) {
        mProgressDialog.cancel();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoPreviewActivity.this, "成功", Toast.LENGTH_SHORT).show();
            }
        });
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/mp4");
        startActivity(intent);
        finish();
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        //mVideoTrimmer.destroy();
        finish();
    }

    @Override
    public void onError(final String message) {
        mProgressDialog.cancel();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoPreviewActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVideoPrepared() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(VideoPreviewActivity.this, "onVideoPrepared", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void init() {

        mHolderTopView = ((SeekBar) findViewById(R.id.handlerTop));
        mVideoProgressIndicator = ((ProgressBarView) findViewById(R.id.timeVideoView));
        mRangeSeekBarView = ((RangeSeekBarView) findViewById(R.id.timeLineBar));
        mLinearVideo = ((RelativeLayout) findViewById(R.id.layout_surface_view));
        mVideoView = findViewById(R.id.video_loader);
        mPlayView = ((ImageView) findViewById(R.id.icon_video_play));
        mTimeInfoContainer = findViewById(R.id.timeText);
        mTextSize = ((TextView) findViewById(R.id.textSize));
        mTextTimeFrame = ((TextView) findViewById(R.id.textTimeSelection));
        mTextTime = ((TextView) findViewById(R.id.textTime));
        mTimeLineView = ((TimeLineView) findViewById(R.id.timeLineView));
        muteVideoImg = findViewById(R.id.muteVideoImg);
//        cutVideoImg = findViewById(R.id.cutVideoImg);
        nextImg = findViewById(R.id.nextImg);
        recordLineLayout1 = findViewById(R.id.recordLineLayout1);
        recordLineLayout2 = findViewById(R.id.recordLineLayout2);
        recordLineLayout3 = findViewById(R.id.recordLineLayout3);
        deleteLineTxt1 = findViewById(R.id.deleteLineTxt1);
        deleteLineTxt2 = findViewById(R.id.deleteLineTxt2);
        deleteLineTxt3 = findViewById(R.id.deleteLineTxt3);
        linearLayout1 = findViewById(R.id.lineLayout1);
        linearLayout2 = findViewById(R.id.lineLayout2);
        linearLayout3 = findViewById(R.id.lineLayout3);
        recordImg = findViewById(R.id.recordImg);
        drawView1 = new DrawView(VideoPreviewActivity.this);
        drawView2 = new DrawView(VideoPreviewActivity.this);
        drawView3 = new DrawView(VideoPreviewActivity.this);
        backTxt = findViewById(R.id.backTxt);


        handler=new Handler();

        setUpListeners();
        setUpMargins();

        linearLayout1.addView(drawView1);
        linearLayout2.addView(drawView2);
        linearLayout3.addView(drawView3);

        deleteLineTxt1.setTypeface(ApplicationService.getFont());
        deleteLineTxt1.setText(R.string.fa_times);
        deleteLineTxt2.setTypeface(ApplicationService.getFont());
        deleteLineTxt2.setText(R.string.fa_times);
        deleteLineTxt3.setTypeface(ApplicationService.getFont());
        deleteLineTxt3.setText(R.string.fa_times);
    }

    private void setUpListeners() {
        mListeners = new ArrayList<>();
        mListeners.add(new OnProgressVideoListener() {
            @Override
            public void updateProgress(int time, int max, float scale) {
                updateVideoProgress(time);
            }
        });
        mListeners.add(mVideoProgressIndicator);

        backTxt.setTypeface(ApplicationService.getFont());
        backTxt.setText(R.string.fa_angle_left);
        backTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.btCancel)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //onCancelClicked();
                            }
                        }
                );

        findViewById(R.id.btSave)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //onSaveClicked();
                            }
                        }
                );

        muteVideoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMute){
                    muteVideoImg.setImageResource(R.drawable.speaker);
                    muteVideoImg.setAlpha(1f);
                    unmute();
                }else{
                    muteVideoImg.setImageResource(R.drawable.speaker_close);
                    muteVideoImg.setAlpha(0.5f);
                    mute();
                }
            }
        });

//        recordBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(recordAudioList.size() < 3){
//                    //limit record time
//                    startRecordTime = currentDuration;
//                    Log.d(TAG, "startRecordTime" + String.valueOf(startRecordTime));
//                    limitRecordTime = (mEndPosition - startRecordTime) / 1000;
//
//                    startRecordAudio();
//                }else{
//                    Toast.makeText(VideoPreviewActivity.this, "已超過錄音數量3個", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        recordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recordAudioList.size() < 3){
                    if(!isRecording){
                        if(!mVideoView.isPlaying()){
                            isRecording = true;

                            //limit record time
                            startRecordTime = currentDuration;
                            Log.d(TAG, "startRecordTime" + String.valueOf(startRecordTime));
                            limitRecordTime = (mEndPosition - startRecordTime) / 1000;

                            //get start coordinate
                            int width = mHolderTopView.getWidth()
                                    - mHolderTopView.getPaddingLeft()
                                    - mHolderTopView.getPaddingRight();

                            if(mResetSeekBar){
                                startRecordCoordinate = 0;
                                mResetSeekBar = false;
                            }else{
                                startRecordCoordinate = width * mHolderTopView.getProgress() / mHolderTopView.getMax();
                            }


                            startRecordAudio();
                        }else{
                            onClickVideoPlayPause();
                        }
                    }else{
                        isRecording = false;
                        stopRecordAudio();
                        //resetAudioPlay();
                    }
                }else{
                    Toast.makeText(VideoPreviewActivity.this, "已超過錄音數量3個", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteLineTxt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.unload(recordAudioList.get(0).id);
                recordAudioList.remove(0);
                if(recordAudioList.size() == 2){
                    deleteLineTxt3.setVisibility(View.GONE);
                    deleteAllLine();
                    drawView1.setSizeColor(drawView2.start, drawView2.mWidth, drawView2.mHeight, Color.parseColor("#33CCFF"));
                    drawView2.setSizeColor(drawView3.start, drawView3.mWidth, drawView3.mHeight, Color.parseColor("#33CCFF"));
                    drawView3 = new DrawView(VideoPreviewActivity.this);
                    addAllLine();
                }else if(recordAudioList.size() == 1){
                    deleteLineTxt3.setVisibility(View.GONE);
                    deleteLineTxt2.setVisibility(View.GONE);
                    deleteAllLine();
                    drawView1.setSizeColor(drawView2.start, drawView2.mWidth, drawView2.mHeight, Color.parseColor("#33CCFF"));
                    drawView2 = new DrawView(VideoPreviewActivity.this);
                    drawView3 = new DrawView(VideoPreviewActivity.this);
                    addAllLine();
                }else{
                    deleteLineTxt3.setVisibility(View.GONE);
                    deleteLineTxt2.setVisibility(View.GONE);
                    deleteLineTxt1.setVisibility(View.GONE);
                    deleteAllLine();
                    drawView1 = new DrawView(VideoPreviewActivity.this);
                    drawView2 = new DrawView(VideoPreviewActivity.this);
                    drawView3 = new DrawView(VideoPreviewActivity.this);
                    addAllLine();
                }
            }
        });

        deleteLineTxt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.unload(recordAudioList.get(1).id);
                recordAudioList.remove(1);
                if(recordAudioList.size() == 2){
                    deleteLineTxt3.setVisibility(View.GONE);
                    deleteAllLine();
                    drawView2.setSizeColor(drawView3.start, drawView3.mWidth, drawView3.mHeight, Color.parseColor("#33CCFF"));
                    drawView3 = new DrawView(VideoPreviewActivity.this);
                    addAllLine();
                }else if(recordAudioList.size() == 1){
                    deleteLineTxt3.setVisibility(View.GONE);
                    deleteLineTxt2.setVisibility(View.GONE);
                    deleteAllLine();
                    drawView2 = new DrawView(VideoPreviewActivity.this);
                    drawView3 = new DrawView(VideoPreviewActivity.this);
                    addAllLine();
                }
            }
        });

        deleteLineTxt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.unload(recordAudioList.get(2).id);
                recordAudioList.remove(2);
                if(recordAudioList.size() == 2){
                    deleteLineTxt3.setVisibility(View.GONE);
                    deleteAllLine();
                    drawView3 = new DrawView(VideoPreviewActivity.this);
                    addAllLine();
                }
            }
        });

        nextImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDialog = showLoadDialog();
                loadFFMpegBinary();
                executeHandleVideoCommand();
//                Intent intent = new Intent();
//                intent.setClass(VideoPreviewActivity.this, VideoContentTitleActivity.class);
//                intent.putExtra("videoSampleId", videoSampleId);
//                startActivity(intent);
            }
        });

        final GestureDetector gestureDetector = new
                GestureDetector(VideoPreviewActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        onClickVideoPlayPause();
                        return true;
                    }
                }
        );

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                if (mOnTrimVideoListener != null)
                    mOnTrimVideoListener.onError("Something went wrong reason : " + what);
                finish();
                return false;
            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
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
        mRangeSeekBarView.addOnRangeSeekBarListener(mVideoProgressIndicator);

        mHolderTopView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onPlayerIndicatorSeekChanged(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onPlayerIndicatorSeekStart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onPlayerIndicatorSeekStop(seekBar);
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;
                onVideoPrepared(mp);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onVideoCompleted();
            }
        });
    }

    private void setUpMargins() {
        int marge = (int)(mRangeSeekBarView.getThumbs().get(0).getWidthBitmap() * 1.5);
        int widthSeek = mHolderTopView.getThumb().getMinimumWidth() / 2;

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHolderTopView.getLayoutParams();
        lp.setMargins(marge - widthSeek, 0, marge - widthSeek, 0);
        mHolderTopView.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) mTimeLineView.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        mTimeLineView.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) mVideoProgressIndicator.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        mVideoProgressIndicator.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) linearLayout1.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        linearLayout1.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) linearLayout2.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        linearLayout2.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) linearLayout3.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        linearLayout3.setLayoutParams(lp);
    }

    private void updateVideoProgress(int time) {
        if (mVideoView == null) {
            return;
        }
//        if (time >= mEndPosition) {
//            mMessageHandler.removeMessages(SHOW_PROGRESS);
//            mVideoView.pause();
//            mPlayView.setVisibility(View.VISIBLE);
//            mResetSeekBar = true;
////            mResetSeekBar = false;
////            mVideoView.seekTo(mStartPosition);
//            return;
//        }

        if (mHolderTopView != null) {
            // use long to avoid overflow
            setProgressBarPosition(time);
        }
        setTimeVideo(time);
    }

    private void setTimeVideo(int position) {
        String seconds = getString(R.string.short_seconds);
//        mTextTime.setText(String.format("%s %s", stringForTime(position), seconds));
        mTextTime.setText(String.format("%s", stringForTime(position)));
        currentDuration = position;
        System.out.println("currentDuration " + currentDuration + " " + mDuration);
    }

    private void deleteAllLine(){
        linearLayout1.removeAllViews();
        linearLayout2.removeAllViews();
        linearLayout3.removeAllViews();
    }

    private void addAllLine(){
        linearLayout1.addView(drawView1);
        linearLayout2.addView(drawView2);
        linearLayout3.addView(drawView3);
    }

//    private void startRecordAudio(){
//        if (mVideoView.isPlaying()) {
//            mPlayView.setVisibility(View.VISIBLE);
//            mMessageHandler.removeMessages(SHOW_PROGRESS);
//            mVideoView.pause();
//            stopRecordAudio();
//        } else {
//            mPlayView.setVisibility(View.GONE);
//
//            if (mResetSeekBar) {
//                mResetSeekBar = false;
//                mVideoView.seekTo(mStartPosition);
//            }
//
//            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
//            mVideoView.start();
//            recordAudio();
//
//        }
//    }

    public void startRecordAudio(){
        mPlayView.setVisibility(View.GONE);
        recordImg.setImageResource(R.drawable.stop_recorder);
//        if (mResetSeekBar) {
//            mResetSeekBar = false;
//            mVideoView.seekTo(mStartPosition);
//        }

        mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
        mVideoView.start();
        recordAudio();
    }

//    public void recordAudio(){
//        if(recordAudioList.size() < 3){
//            if(!isRecording){
//                this.setVolume(0);
//                mVideoView.setEnabled(false);
//
//                //Create MediaRecorder and initialize audio source, output format, and audio encoder
//                mRecorder = new MediaRecorder();
//                mRecorder.reset();
//                mRecorder.setAudioSource( MediaRecorder.AudioSource.MIC);
//                mRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP);
//
//                if(recordAudioList.size() == 0){
//                    fileName= Environment.getExternalStorageDirectory()+ "/audio1" +".mp3";
//                }else if(recordAudioList.size() == 1){
//                    fileName=Environment.getExternalStorageDirectory()+ "/audio2" +".mp3";
//                }else if(recordAudioList.size() == 2){
//                    fileName=Environment.getExternalStorageDirectory()+ "/audio3" +".mp3";
//                }
//                mRecorder.setOutputFile(fileName);
//                mRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB);
//                // Starting record time
//                recordTime=0;
//
//                try {
//                    mRecorder.prepare();
//                } catch (IOException e) {
//                    Log.e("LOG_TAG", "prepare failed");
//                }
//                // Start record job
//                mRecorder.start();
//                // Change isRecroding flag to true
//                isRecording=true;
//                // Post the record progress
//                handler.post(UpdateRecordTime);
//            }
//        }else{
//            Toast.makeText(this, "超過數量 " + recordAudioList.get(0) + "\n" + recordAudioList.get(1) + "\n" + recordAudioList.get(2), Toast.LENGTH_SHORT).show();
//        }
//    }

    public void recordAudio(){
        this.setVolume(0);
        mVideoView.setEnabled(false);
        mHolderTopView.setEnabled(false);
        mRangeSeekBarView.setEnabled(false);

        //Create MediaRecorder and initialize audio source, output format, and audio encoder
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource( MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP);

        if(recordAudioList.size() == 0){
            fileName= Environment.getExternalStorageDirectory()+ "/audio1" +".mp3";
        }else if(recordAudioList.size() == 1){
            fileName=Environment.getExternalStorageDirectory()+ "/audio2" +".mp3";
        }else if(recordAudioList.size() == 2){
            fileName=Environment.getExternalStorageDirectory()+ "/audio3" +".mp3";
        }
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB);
        // Starting record time
        recordTime=0;

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare failed");
        }
        // Start record job
        mRecorder.start();
//
//        // Post the record progress
//        handler.post(UpdateRecordTime);
    }

//    public void stopRecordAudio(){
//        if(isRecording){
//            if(!isMute){
//                this.setVolume(100);
//            }
//            mVideoView.setEnabled(true);
//
//            endRecordTime = currentDuration;
//            Log.d(TAG, "endRecordTime" + endRecordTime);
//            // Stop recording and release resource
//            mRecorder.stop();
//            mRecorder.release();
//            mRecorder = null;
//            // Change isRecording flag to false
//            isRecording=false;
//
//            RecordAudio recordAudio = new RecordAudio();
//            recordAudio.id = soundPool.load(fileName,0);
//            recordAudio.path = fileName;
//            recordAudio.startTime = startRecordTime / 1000;
//            recordAudio.endTime = endRecordTime / 1000;
//
//            recordAudioList.add(recordAudio);
//
//            if(recordAudioList.size() == 1){
//                deleteFirstBtn.setVisibility(View.VISIBLE);
//            }else if(recordAudioList.size() == 2){
//                deleteSecondBtn.setVisibility(View.VISIBLE);
//            }else if(recordAudioList.size() == 3){
//                deleteThirdBtn.setVisibility(View.VISIBLE);
//            }
//
//            //回到起點
//            mResetSeekBar = false;
//            mVideoView.seekTo(mStartPosition);
//        }
//    }

    public void stopRecordAudio(){
        recordImg.setImageResource(R.drawable.recorder);
        if(mRecorder != null){
            if(!isMute){
                this.setVolume(100);
            }
            mVideoView.setEnabled(true);
            mHolderTopView.setEnabled(true);

            endRecordTime = currentDuration;
            Log.d(TAG, "startRecordTime" + startRecordTime);
            Log.d(TAG, "endRecordTime" + endRecordTime);
            // Stop recording and release resource
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            RecordAudio recordAudio = new RecordAudio();
            recordAudio.id = soundPool.load(fileName,0);
            recordAudio.path = fileName;
            if(startRecordTime == 0){
                recordAudio.startTime = startRecordTime + 1;
            }else{
                recordAudio.startTime = startRecordTime;
            }

            if(endRecordTime == 0){
                recordAudio.endTime = endRecordTime + 1;
            }else{
                recordAudio.endTime = endRecordTime;
            }

            recordAudioList.add(recordAudio);

            if(recordAudioList.size() == 1){
                deleteLineTxt1.setVisibility(View.VISIBLE);
            }else if(recordAudioList.size() == 2){
                deleteLineTxt2.setVisibility(View.VISIBLE);
            }else if(recordAudioList.size() == 3){
                deleteLineTxt3.setVisibility(View.VISIBLE);
            }
        }

        //回到起點
        isRecording = false;
//        mResetSeekBar = false;
        mVideoView.seekTo(mStartPosition);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);
        resetAudioPlay();//可重新播放聲音
        currentDuration = 0;
    }

    public void stopRecordAudioAuto(){
        recordImg.setImageResource(R.drawable.recorder);
        if(!isMute){
            this.setVolume(100);
        }
        mVideoView.setEnabled(true);
        mHolderTopView.setEnabled(true);

        endRecordTime = currentDuration;
        Log.d(TAG, "endRecordTime" + endRecordTime);
        // Stop recording and release resource
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        RecordAudio recordAudio = new RecordAudio();
        recordAudio.id = soundPool.load(fileName,0);
        recordAudio.path = fileName;
        if(startRecordTime == 0){
            recordAudio.startTime = startRecordTime + 1;
        }else{
            recordAudio.startTime = startRecordTime;
        }

        if(endRecordTime == 0){
            recordAudio.endTime = endRecordTime + 1;
        }else{
            recordAudio.endTime = endRecordTime;
        }

        recordAudioList.add(recordAudio);

        if(recordAudioList.size() == 1){
            deleteLineTxt1.setVisibility(View.VISIBLE);
        }else if(recordAudioList.size() == 2){
            deleteLineTxt2.setVisibility(View.VISIBLE);
        }else if(recordAudioList.size() == 3){
            deleteLineTxt3.setVisibility(View.VISIBLE);
        }

        //回到起點
        isRecording = false;
//        mResetSeekBar = false;
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);
        resetAudioPlay();//可重新播放聲音
        currentDuration = 0;
    }

    private void onClickVideoPlayPause() {
        if (mVideoView.isPlaying()) {
            mPlayView.setVisibility(View.VISIBLE);
            mMessageHandler.removeMessages(SHOW_PROGRESS);
            mVideoView.pause();
        } else {
//            handler.post(TimeForAudio);
            mPlayView.setVisibility(View.GONE);
            if (mResetSeekBar) {
                mResetSeekBar = false;
                mVideoView.seekTo(mStartPosition);
            }

            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
            mVideoView.start();
        }
    }

    private void onPlayerIndicatorSeekChanged(int progress, boolean fromUser) {

        int duration = (int) ((mDuration * progress) / 1000L);

        if (fromUser) {
            if (duration < mStartPosition) {
                setProgressBarPosition(mStartPosition);
                duration = mStartPosition;
            } else if (duration > mEndPosition) {
                setProgressBarPosition(mEndPosition);
                duration = mEndPosition;
            }
            setTimeVideo(duration);
        }
    }

    private void onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);
        notifyProgressUpdate(false);
    }

    private void onPlayerIndicatorSeekStop(@NonNull SeekBar seekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);

        int duration = (int) ((mDuration * seekBar.getProgress()) / 1000L);
        mVideoView.seekTo(duration);
        setTimeVideo(duration);
        notifyProgressUpdate(false);
        resetAudioPlay();//可重新播放聲音
    }

    private void onVideoPrepared(@NonNull MediaPlayer mp) {
        // Adjust the size of the video
        // so it fits on the screen
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = mLinearVideo.getWidth();
        int screenHeight = mLinearVideo.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        mVideoView.setLayoutParams(lp);

        mPlayView.setVisibility(View.VISIBLE);

        mDuration = mVideoView.getDuration();
        setSeekBarPosition();

//        setTimeFrames();
        setTimeVideo(0);

        if (mOnK4LVideoListener != null) {
            mOnK4LVideoListener.onVideoPrepared();
        }
        if(isMute){
            mute();
        }else{
            unmute();
        }
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
        mVideoView.seekTo(mStartPosition);

        mTimeVideo = mDuration;
        mRangeSeekBarView.initMaxWidth();
    }

    private void setProgressBarPosition(int position) {
        if (mDuration > 0) {
            long pos = 1000L * position / mDuration;
            mHolderTopView.setProgress((int) pos);


            int[] location = new int[2];
            mHolderTopView.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];

            int width = mHolderTopView.getWidth()
                    - mHolderTopView.getPaddingLeft()
                    - mHolderTopView.getPaddingRight();
            int thumbPos = mHolderTopView.getPaddingLeft()
                    + width
                    * mHolderTopView.getProgress()
                    / mHolderTopView.getMax();

            if(isRecording){
                if(recordAudioList.size() == 0){
                    drawView1.setSizeColor(startRecordCoordinate, width
                            * mHolderTopView.getProgress()
                            / mHolderTopView.getMax(), y, Color.parseColor("#33CCFF"));
                    drawView1.setAlpha(0.5f);
                    drawView1.invalidate();
                }else if(recordAudioList.size() == 1){
                    drawView2.setSizeColor(startRecordCoordinate, width
                            * mHolderTopView.getProgress()
                            / mHolderTopView.getMax(), y, Color.parseColor("#33CCFF"));
                    drawView2.setAlpha(0.7f);
                    drawView2.invalidate();
                }else if(recordAudioList.size() == 2){
                    drawView3.setSizeColor(startRecordCoordinate, width
                            * mHolderTopView.getProgress()
                            / mHolderTopView.getMax(), y, Color.parseColor("#33CCFF"));
                    drawView3.invalidate();
                    drawView3.setAlpha(0.9f);
                }

//                if(!mVideoView.isPlaying()){
//                    stopRecordAudio();
//                    resetAudioPlay();
//                }
            }else{
//                int integerCurrentDuration = currentDuration / 1000;
                if(recordAudioList.size() == 1){
                    if(!firstAudioPlay){
                        if(currentDuration >= recordAudioList.get(0).startTime){
//                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                            soundPool.play(recordAudioList.get(0).id, 1, 1, 1, 0, 1);
                            firstAudioPlay = true;
                        }
                    }

                }else if(recordAudioList.size() == 2){
                    if(!firstAudioPlay){
                        if(currentDuration >= recordAudioList.get(0).startTime){
//                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                            soundPool.play(recordAudioList.get(0).id, 1, 1, 1, 0, 1);
                            firstAudioPlay = true;
                        }
                    }

                    if(!secondAudioPlay){
                        if(currentDuration >= recordAudioList.get(1).startTime){
//                        soundPool .play(recordAudioList.get(1).id,1,1, 0, 0, 1);
                            soundPool.play(recordAudioList.get(1).id, 1, 1, 1, 0, 1);
                            secondAudioPlay = true;
                        }
                    }
                }else if(recordAudioList.size() == 3){
                    if(!firstAudioPlay){
                        if(currentDuration >= recordAudioList.get(0).startTime){
//                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                            soundPool.play(recordAudioList.get(0).id, 1, 1, 1, 0, 1);
                            firstAudioPlay = true;
                        }
                    }
                    if(!secondAudioPlay){
                        if(currentDuration >= recordAudioList.get(1).startTime){
//                        soundPool .play(recordAudioList.get(1).id,1,1, 0, 0, 1);
                            soundPool.play(recordAudioList.get(1).id, 1, 1, 1, 0, 1);
                            secondAudioPlay = true;
                        }
                    }
                    if(!thirdAudioPlay){
                        if(currentDuration >= recordAudioList.get(2).startTime){
//                        soundPool .play(recordAudioList.get(2).id,1,1, 0, 0, 1);
                            soundPool.play(recordAudioList.get(2).id, 1, 1, 1, 0, 1);
                            thirdAudioPlay = true;
                        }
                    }
                }

//                if(!mVideoView.isPlaying()){
//                    mPlayView.setVisibility(View.VISIBLE);
//                    resetAudioPlay();
//                }
            }


        }
    }

    private void onSeekThumbs(int index, float value) {
        switch (index) {
            case Thumb.LEFT: {
                mStartPosition = (int) ((mDuration * value) / 100L);
                mVideoView.seekTo(mStartPosition);
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
        mMessageHandler.removeMessages(SHOW_PROGRESS);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);
    }

    private void onVideoCompleted() {
        if(isRecording){
            stopRecordAudioAuto();
        }
        mPlayView.setVisibility(View.VISIBLE);
        resetAudioPlay();
        currentDuration = 0;
        mResetSeekBar = true;
    }

    private void    notifyProgressUpdate(boolean all) {
        if (mDuration == 0) return;

        int position = mVideoView.getCurrentPosition();
        if (all) {
            for (OnProgressVideoListener item : mListeners) {
                item.updateProgress(position, mDuration, ((position * 100) / mDuration));
            }
        } else {
            mListeners.get(1).updateProgress(position, mDuration, ((position * 100) / mDuration));
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(VideoPreviewActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VideoPreviewActivity.this.finish();
                    }
                })
                .create()
                .show();
    }

    //    /**
//     * Load FFmpeg binary
//     */
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
                    Intent intent = new Intent();
                    intent.setClass(VideoPreviewActivity.this, VideoContentTitleActivity.class);
                    startActivity(intent);
                    closeLoadDialog();
                }

                @Override
                public void onProgress(String s) {
//                    Log.d(TAG, "Started progress : ffmpeg " + s);
//                    int start = s.indexOf("time=");
//                    int end = s.indexOf(" bitrate");
//                    if (start != -1 && end != -1) {
//                        String duration = s.substring(start + 5, end);
//                        if (duration != "") {
//                            try {
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
////                                dialog.setProgress((int)sdf.parse("1970-01-01 " + duration).getTime());
//                                Log.d(TAG, "Started progress : ffmpeg " + (int)sdf.parse("1970-01-01 " + duration).getTime());
//                            }catch (ParseException e)
//                            {
//                                e.printStackTrace();
//                            }
//                        }
//                    }

                    if(!isMute){
                        Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command));
                        Log.d(TAG, "progress : " + s);
                        Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
                        Scanner sc = new Scanner(s);

                        String match = sc.findWithinHorizon(timePattern, 0);
                        if (match != null) {
                            String[] matchSplit = match.split(":");
                            if (mDuration != 0) {
                                float progress = (Integer.parseInt(matchSplit[0]) * 3600 +
                                        Integer.parseInt(matchSplit[1]) * 60 +
                                        Float.parseFloat(matchSplit[2])) / mDuration;
                                float showProgress = (progress * 100) * 1000;
                                Log.d(TAG, "=======PROGRESS======== " + showProgress);
                                TextView loadingTitleTxt = loadDialog.findViewById(R.id.loadingTitleTxt);
                                loadingTitleTxt.setText("影片合成中");
                                NumberProgressBar number_progress_bar = loadDialog.findViewById(R.id.number_progress_bar);
                                number_progress_bar.setProgress((int)showProgress);
                            }
                        }
                    }
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

    public void executeHandleVideoCommand(){
        File moviesDir = new File(ApplicationParameter.FILE_FOLDER_SAVE_PATH);
        String filePrefix = "share_content1";
        String fileExtn = ".mp4";
        originalFileName = ApplicationParameter.FILE_SAVE_PATH;
        File dest = new File(moviesDir, filePrefix + fileExtn);
//        int fileNo = 0;
//        while (dest.exists()) {
//            fileNo++;
//            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
//        }
        handleFileName = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", originalFileName, "-an", "-c", "copy", handleFileName};

        if(isMute){
            String[] complexCommand = {"-y", "-i", originalFileName, "-an", "-c", "copy", handleFileName};
            execMuteVideoFFmpegBinary(complexCommand);
        }else{
            switch(recordAudioList.size()){
                case 0:{
                    File file = new File(originalFileName);
                    File newFile = new File(ApplicationParameter.FINALLY_FILE_SAVE_PATH);
                    try{
                        copy(file, newFile);
                        Intent intent = new Intent();
                        intent.setClass(VideoPreviewActivity.this, VideoContentTitleActivity.class);
                        startActivity(intent);
                        closeLoadDialog();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
                case 1:{
                    String[] complexCommand  = {"-y", "-i", originalFileName, "-i", recordAudioList.get(0).path, "-filter_complex", "[1]adelay=" + recordAudioList.get(0).startTime + "|" + recordAudioList.get(0).startTime + "[aud];[0][aud]amix", "-c:v", "copy", ApplicationParameter.FINALLY_FILE_SAVE_PATH};
                    execFFmpegBinary(complexCommand);
                    break;
                }
                case 2:{
                    String[] complexCommand = {"-y", "-i", originalFileName, "-i", recordAudioList.get(0).path, "-i", recordAudioList.get(1).path, "-filter_complex",
                            "[1]adelay=" + recordAudioList.get(0).startTime  + "|" + recordAudioList.get(0).startTime  + "[aud1];[2]adelay=" + recordAudioList.get(1).startTime  + "|" + recordAudioList.get(1).startTime + "[aud2];[0][aud1][aud2]amix=3", "-c:v", "copy", ApplicationParameter.FINALLY_FILE_SAVE_PATH};
                    execFFmpegBinary(complexCommand);
                    break;
                }
                case 3:{
                String[] complexCommand = {"-y", "-i", originalFileName, "-i", recordAudioList.get(0).path, "-i", recordAudioList.get(1).path, "-i", recordAudioList.get(2).path, "-filter_complex",
                        "[1]adelay=" + recordAudioList.get(0).startTime + "|" + recordAudioList.get(0).startTime + "[aud1];[2]adelay=" + recordAudioList.get(1).startTime + "|" + recordAudioList.get(1).startTime +
                                "[aud2];[3]adelay=" + recordAudioList.get(2).startTime + "|" + recordAudioList.get(2).startTime + "[aud3];[0][aud1][aud2][aud3]amix=4", "-c:v", "copy", ApplicationParameter.FINALLY_FILE_SAVE_PATH};
                    execFFmpegBinary(complexCommand);
                    break;
                }
            }
        }
    }

    private void execMuteVideoFFmpegBinary(final String[] command) {
        if(isMute){
            try {
                ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String s) {
                        Log.d(TAG, "FAILED with output : " + s);
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "SUCCESS with output : " + s);
                        switch(recordAudioList.size()){
                            case 0:{
                                Intent intent = new Intent();
                                intent.setClass(VideoPreviewActivity.this, VideoContentTitleActivity.class);
                                startActivity(intent);
                                break;
                            }
                            case 1:{
//                                String[] complexCommand = {"-y", "-i", originalFileName, "-i", recordAudioList.get(0).path, "-filter_complex",
//                                        "aevalsrc=0:d=" + Math.round(recordAudioList.get(0).startTime / 1000) + "[s1];[s1][1:a]concat=n=2:v=0:a=1[aout]", "-c:v", "copy", "-map", "0:v", "-map", "[aout]", handleFileName};
//                                execFFmpegBinary(complexCommand);

                                String ggFile = "/storage/emulated/0/CrazyTalk/fuck.mp4";
                                String[] complexCommand = {"-y", "-i", handleFileName, "-f", "lavfi", "-i", "anullsrc=cl=1", "-shortest", "-c:v", "libx264", "-c:a", "aac", ggFile};
                                execAddSilentAudioFFmpegBinary(complexCommand);

                                break;
                            }
                            case 2:{
                                String ggFile = "/storage/emulated/0/CrazyTalk/fuck.mp4";
                                String[] complexCommand = {"-y", "-i", handleFileName, "-f", "lavfi", "-i", "anullsrc=cl=1", "-shortest", "-c:v", "libx264", "-c:a", "aac", ggFile};
                                execAddSilentAudioFFmpegBinary(complexCommand);
                                break;
                            }
                            case 3:{
                                String ggFile = "/storage/emulated/0/CrazyTalk/fuck.mp4";
                                String[] complexCommand = {"-y", "-i", handleFileName, "-f", "lavfi", "-i", "anullsrc=cl=1", "-shortest", "-c:v", "libx264", "-c:a", "aac", ggFile};
                                execAddSilentAudioFFmpegBinary(complexCommand);

                                break;
                            }
                        }
                    }

                    @Override
                    public void onProgress(String s) {
//                        Log.d(TAG, "Started progress : ffmpeg " + s);
                        int start = s.indexOf("time=");
                        int end = s.indexOf(" bitrate");
                        if (start != -1 && end != -1) {
                            String duration = s.substring(start + 5, end);
                            if (duration != "") {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                                dialog.setProgress((int)sdf.parse("1970-01-01 " + duration).getTime());
                                    Log.d(TAG, "Started progress : ffmpeg " + (int)sdf.parse("1970-01-01 " + duration).getTime());
                                }catch (ParseException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
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
        }else{
//            String[] complexCommand = {"-ss", "" + mStartPosition / 1000, "-y", "-i", handleFileName, "-t", "" + (mEndPosition - mStartPosition) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", handleFileName};
//            String[] complexCommand  = {"-i", handleFileName, "-i", recordAudioList.get(0).path, "-filter_complex", "[1]adelay=1000|1000[aud];[0][aud]amix", "-c:v", "copy", handleFileName};
//            execCutVideoFFmpegBinary(complexCommand);
        }
    }

    private void execAddSilentAudioFFmpegBinary(final String[] command){
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);
                    String processFile = "/storage/emulated/0/CrazyTalk/fuck.mp4";

                    if(recordAudioList.size() == 1){
                        String[] complexCommand  = {"-y", "-i", processFile, "-i", recordAudioList.get(0).path, "-filter_complex", "[1]adelay=" + recordAudioList.get(0).startTime + "|" + recordAudioList.get(0).startTime + "[aud];[0][aud]amix", "-c:v", "copy",  ApplicationParameter.FINALLY_FILE_SAVE_PATH};

                        execFFmpegBinary(complexCommand);
                    }else if(recordAudioList.size() == 2){
                        String[] complexCommand = {"-y", "-i", processFile, "-i", recordAudioList.get(0).path, "-i", recordAudioList.get(1).path, "-filter_complex",
                                "[1]adelay=" + recordAudioList.get(0).startTime  + "|" + recordAudioList.get(0).startTime  + "[aud1];[2]adelay=" + recordAudioList.get(1).startTime  + "|" + recordAudioList.get(1).startTime + "[aud2];[0][aud1][aud2]amix=3", "-c:v", "copy", ApplicationParameter.FINALLY_FILE_SAVE_PATH};

                        execFFmpegBinary(complexCommand);
                    }else if(recordAudioList.size() == 3){
                        String[] complexCommand = {"-y", "-i", processFile, "-i", recordAudioList.get(0).path, "-i", recordAudioList.get(1).path, "-i", recordAudioList.get(2).path, "-filter_complex",
                                "[1]adelay=" +  recordAudioList.get(0).startTime + "|" + recordAudioList.get(0).startTime + "[aud1];[2]adelay=" + recordAudioList.get(1).startTime + "|" + recordAudioList.get(1).startTime +
                                        "[aud2];[3]adelay=" + recordAudioList.get(2).startTime + "|" + recordAudioList.get(2).startTime + "[aud3];[0][aud1][aud2][aud3]amix=4", "-c:v", "copy",  ApplicationParameter.FINALLY_FILE_SAVE_PATH};

                        execFFmpegBinary(complexCommand);
                    }
                }

                @Override
                public void onProgress(String s) {
//                        Log.d(TAG, "Started progress : ffmpeg " + s);
//                    int start = s.indexOf("time=");
//                    int end = s.indexOf(" bitrate");
//                    if (start != -1 && end != -1) {
//                        String duration = s.substring(start + 5, end);
//                        if (duration != "") {
//                            try {
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
////                                dialog.setProgress((int)sdf.parse("1970-01-01 " + duration).getTime());
//                                Log.d(TAG, "AddSilentAudio Started progress : ffmpeg " + (int)sdf.parse("1970-01-01 " + duration).getTime());
//                            }catch (ParseException e)
//                            {
//                                e.printStackTrace();
//                            }
//                        }
//                    }

                    Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command));
                    Log.d(TAG, "progress : " + s);
                    Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
                    Scanner sc = new Scanner(s);

                    String match = sc.findWithinHorizon(timePattern, 0);
                    if (match != null) {
                        String[] matchSplit = match.split(":");
                        if (mDuration != 0) {
                            float progress = (Integer.parseInt(matchSplit[0]) * 3600 +
                                    Integer.parseInt(matchSplit[1]) * 60 +
                                    Float.parseFloat(matchSplit[2])) / mDuration;
                            float showProgress = (progress * 100) * 1000;
                            Log.d(TAG, "=======PROGRESS======== " + showProgress);
                            TextView loadingTitleTxt = loadDialog.findViewById(R.id.loadingTitleTxt);
                            loadingTitleTxt.setText("影片合成中");
                            NumberProgressBar number_progress_bar = loadDialog.findViewById(R.id.number_progress_bar);
                            number_progress_bar.setProgress((int)showProgress);
                        }
                    }
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

    private void execCutVideoFFmpegBinary(final String[] command) {
        if(isCut){
            try {
                ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String s) {
                        Log.d(TAG, "FAILED with output : " + s);
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "SUCCESS with output : " + s);
                    }

                    @Override
                    public void onProgress(String s) {
                        Log.d(TAG, "Started progress : ffmpeg " + s);
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
        }else{

        }
    }

    public void mute() {
        this.setVolume(0);
        isMute = true;
    }

    public void unmute() {
        this.setVolume(100);
        isMute = false;
    }

    private void setVolume(int amount) {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        this.mediaPlayer.setVolume(volume, volume);
    }

    private void resetAudioPlay(){
        firstAudioPlay = false;
        secondAudioPlay = false;
        thirdAudioPlay = false;
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }


    /**
     * Listener for events such as trimming operation success and cancel
     *
     * @param onTrimVideoListener interface for events
     */
    @SuppressWarnings("unused")
    public void setOnTrimVideoListener(OnTrimVideoListener onTrimVideoListener) {
        mOnTrimVideoListener = onTrimVideoListener;
    }

    /**
     * Listener for some {@link VideoView} events
     *
     * @param onK4LVideoListener interface for events
     */
    @SuppressWarnings("unused")
    public void setOnK4LVideoListener(OnK4LVideoListener onK4LVideoListener) {
        mOnK4LVideoListener = onK4LVideoListener;
    }

    /**
     * Set the maximum duration of the trimmed video.
     * The trimmer interface wont allow the user to set duration longer than maxDuration
     *
     * @param maxDuration the maximum duration of the trimmed video in seconds
     */
    @SuppressWarnings("unused")
    public void setMaxDuration(int maxDuration) {
        mMaxDuration = maxDuration * 1000;
    }

    /**
     * Sets the uri of the video to be trimmer
     *
     * @param videoURI Uri of the video
     */
    @SuppressWarnings("unused")
    public void setVideoURI(final Uri videoURI) {
        mSrc = videoURI;

        if (mOriginSizeFile == 0) {
            File file = new File(mSrc.getPath());

            mOriginSizeFile = file.length();
            long fileSizeInKB = mOriginSizeFile / 1024;

//            if (fileSizeInKB > 1000) {
//                long fileSizeInMB = fileSizeInKB / 1024;
//                mTextSize.setText(String.format("%s %s", fileSizeInMB, getString(R.string.megabyte)));
//            } else {
//                mTextSize.setText(String.format("%s %s", fileSizeInKB, getString(R.string.kilobyte)));
//            }
        }

        mVideoView.setVideoURI(mSrc);
        mVideoView.requestFocus();

        mTimeLineView.setVideo(mSrc);
    }


    private class MessageHandler extends Handler {


        MessageHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            //K4LVideoTrimmer view = mView.get();
            if (mVideoView == null) {
                return;
            }

            notifyProgressUpdate(true);
            if (mVideoView.isPlaying()) {
                sendEmptyMessageDelayed(0, 10);
            }
        }
    }
}
