package com.example.john.mimicvideo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.john.mimicvideo.model.RecordAudio;
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

import junit.framework.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.example.john.mimicvideo.utils.MyProgressDialog.show;
import static com.example.john.mimicvideo.view.VideoTrimmer.utils.TrimVideoUtils.stringForTime;

public class TestVideoActivity extends BaseActivity implements OnTrimVideoListener, OnK4LVideoListener{

    private static final String TAG = TestVideoActivity.class.getSimpleName();
    private static final int MIN_TIME_FRAME = 1000;
    private static final int SHOW_PROGRESS = 2;

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
    private Button recordBtn;
    private ImageView cutVideoImg;
    private Button deleteFirstBtn;
    private Button deleteSecondBtn;
    private Button deleteThirdBtn;
    private ImageView nextImg;

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
    private int currentDuration;
    private boolean isMute = true;
    private boolean isCut = true;

    private long mOriginSizeFile;
    private boolean mResetSeekBar = true;
    private final MessageHandler mMessageHandler = new MessageHandler();

    private ProgressDialog mProgressDialog;

    //below record audio
    List<RecordAudio>recordAudioList = new ArrayList<>();
    private boolean isRecording = false;
    MediaRecorder mRecorder;
    private String fileName;
    private SoundPool soundPool ;
    private int recordTime = 0;
    private Handler handler;
    private int limitRecordTime = 0;
    private int startRecordTime = 0;
    private int endRecordTime = 0;

    //below handle video
    private FFmpeg ffmpeg;
    private String originalFileName;
    private String handleFileName;

    //below mute video
    private MediaPlayer mediaPlayer = new MediaPlayer();

    int videoSampleId = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("合成中");

        if(getIntent().getIntExtra("video_sample_id", 0) != 0){
            videoSampleId = getIntent().getIntExtra("video_sample_id", 0);
            String videoSampleUrl = getIntent().getStringExtra("video_sample_url");
            //new DownloadFileFromURL(videoSampleUrl);
        }else{
            String videoContentUrl = getIntent().getStringExtra("videoContentUrl");
            //new DownloadFileFromURL(videoContentUrl).execute();
        }

        init();
        setVideoURI(Uri.parse( "/storage/emulated/0/share_content.mp4"));
        setMaxDuration(40);
        setOnTrimVideoListener(this);
        setOnK4LVideoListener(this);
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
                Toast.makeText(TestVideoActivity.this, "成功", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(TestVideoActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVideoPrepared() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestVideoActivity.this, "onVideoPrepared", Toast.LENGTH_SHORT).show();
            }
        });
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
        recordBtn = findViewById(R.id.recordBtn);
        cutVideoImg = findViewById(R.id.cutVideoImg);
        deleteFirstBtn = findViewById(R.id.deleteFirstBtn);
        deleteSecondBtn = findViewById(R.id.deleteSecondBtn);
        deleteThirdBtn = findViewById(R.id.deleteThirdBtn);
        nextImg = findViewById(R.id.nextImg);

        handler=new Handler();
        soundPool = new SoundPool(100, 0,10);
        loadFFMpegBinary();
        //executeHandleVideoCommand();

        setUpListeners();
        setUpMargins();
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
                    unmute();
                }else{
                    muteVideoImg.setImageResource(R.drawable.speaker_close);
                    mute();
                }
                //executeHandleVideoCommand();
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recordAudioList.size() < 3){
                    //limit record time
                    startRecordTime = currentDuration;
                    Log.d(TAG, "startRecordTime" + String.valueOf(startRecordTime));
                    limitRecordTime = (mEndPosition - startRecordTime) / 1000;

                    startRecordAudio();
                }else{
                    Toast.makeText(TestVideoActivity.this, "已超過錄音數量3個", Toast.LENGTH_SHORT).show();
                }
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

        nextImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                executeMuteVideoCommand(Uri.parse("/storage/emulated/0/share_content.mp4"));
                executeAddAudioToVideoCommand(Uri.parse("/storage/emulated/0/fuck_video.mp4"));
            }
        });

        final GestureDetector gestureDetector = new
                GestureDetector(TestVideoActivity.this,
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
        int marge = mRangeSeekBarView.getThumbs().get(0).getWidthBitmap();
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
    }

    private void updateVideoProgress(int time) {
        if (mVideoView == null) {
            return;
        }

        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS);
            mVideoView.pause();
            mPlayView.setVisibility(View.VISIBLE);
            mResetSeekBar = true;
            return;
        }

        if (mHolderTopView != null) {
            // use long to avoid overflow
            setProgressBarPosition(time);
        }
        setTimeVideo(time);
    }

    private void setTimeVideo(int position) {
        String seconds = getString(R.string.short_seconds);
        mTextTime.setText(String.format("%s %s", stringForTime(position), seconds));
        currentDuration = position;
    }

    private void startRecordAudio(){
        if (mVideoView.isPlaying()) {
            mPlayView.setVisibility(View.VISIBLE);
            mMessageHandler.removeMessages(SHOW_PROGRESS);
            mVideoView.pause();
            stopRecordAudio();
        } else {
            mPlayView.setVisibility(View.GONE);

            if (mResetSeekBar) {
                mResetSeekBar = false;
                mVideoView.seekTo(mStartPosition);
            }

            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
            mVideoView.start();
            recordAudio();

        }
    }

    public void recordAudio(){
        if(recordAudioList.size() < 3){
            if(!isRecording){
                this.setVolume(0);
                mVideoView.setEnabled(false);

                //Create MediaRecorder and initialize audio source, output format, and audio encoder
                mRecorder = new MediaRecorder();
                mRecorder.reset();
                mRecorder.setAudioSource( MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP);

                if(recordAudioList.size() == 0){
//                    fileName=Environment.getExternalStorageDirectory()+ "/audio"+System.currentTimeMillis()+".mp3";
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
                // Change isRecroding flag to true
                isRecording=true;
                // Post the record progress
                handler.post(UpdateRecordTime);
            }
        }else{
            Toast.makeText(this, "超過數量 " + recordAudioList.get(0) + "\n" + recordAudioList.get(1) + "\n" + recordAudioList.get(2), Toast.LENGTH_SHORT).show();
        }
    }

    public void stopRecordAudio(){
        if(isRecording){
            this.setVolume(100);
            mVideoView.setEnabled(true);

            endRecordTime = currentDuration;
            Log.d(TAG, "endRecordTime" + endRecordTime);
            // Stop recording and release resource
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            // Change isRecording flag to false
            isRecording=false;

            RecordAudio recordAudio = new RecordAudio();
            recordAudio.id = soundPool.load(fileName,0);
            recordAudio.path = fileName;
            recordAudio.startTime = startRecordTime / 1000;
            recordAudio.endTime = endRecordTime / 1000;

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

    Runnable UpdateRecordTime=new Runnable(){
        public void run(){
            if(isRecording){
                if(recordTime == limitRecordTime){
                    stopRecordAudio();
                }
                recordTime+=1;
                // Delay 1s before next call
                handler.postDelayed(this, 1000);
            }
        }
    };

    Runnable TimeForAudio = new Runnable() {
        @Override
        public void run() {
            if(mVideoView.isPlaying()){
                // Delay 1s before next call
                int integerCurrentDuration = currentDuration / 1000;
                if(recordAudioList.size() == 1){
                    if(integerCurrentDuration == recordAudioList.get(0).startTime){
                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                    }
                }else if(recordAudioList.size() == 2){
                    if(integerCurrentDuration == recordAudioList.get(0).startTime){
                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                    }
                    if(integerCurrentDuration == recordAudioList.get(1).startTime){
                        soundPool .play(recordAudioList.get(1).id,1,1, 0, 0, 1);
                    }
                }else if(recordAudioList.size() == 3){
                    if(integerCurrentDuration == recordAudioList.get(0).startTime){
                        System.out.println("banana");
                        soundPool .play(recordAudioList.get(0).id,1,1, 0, 0, 1);
                    }
                    if(integerCurrentDuration == recordAudioList.get(1).startTime){
                        soundPool .play(recordAudioList.get(1).id,1,1, 0, 0, 1);
                    }
                    if(integerCurrentDuration == recordAudioList.get(2).startTime){
                        soundPool .play(recordAudioList.get(2).id,1,1, 0, 0, 1);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void onClickVideoPlayPause() {
        handler.post(TimeForAudio);
        if (mVideoView.isPlaying()) {
            mPlayView.setVisibility(View.VISIBLE);
            mMessageHandler.removeMessages(SHOW_PROGRESS);
            mVideoView.pause();
        } else {
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
        mVideoView.seekTo(mStartPosition);
    }

    private void notifyProgressUpdate(boolean all) {
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
        new AlertDialog.Builder(TestVideoActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TestVideoActivity.this.finish();
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
    }

    //comment for add audio to specific video position
    private void executeAddAudioToVideoCommand(Uri selectedVideoUri){
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "gg_video";
        String fileExtn = ".mp4";
        String yourRealPath = "/storage/emulated/0/share_content.mp4";
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
        String videoContentPath = dest.getAbsolutePath();
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
        String filePrefix = "fuck_video";
        String fileExtn = ".mp4";
        String yourRealPath = "/storage/emulated/0/fuck_video.mp4";
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

    public void executeHandleVideoCommand(){
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "fuck";
        String fileExtn = ".mp4";
        originalFileName = "/storage/emulated/0/cut_video.mp4";
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
        handleFileName = dest.getAbsolutePath();
//        String[] complexCommand = {"-i", originalFileName, "-an", "-c", "copy", handleFileName};
        //String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        String[] complexCommand  = {"-i", originalFileName, "-i", recordAudioList.get(0).path, "-filter_complex", "[1]adelay=1000|1000[aud];[0][aud]amix", "-c:v", "copy", handleFileName};
//                execFFmpegBinary(complexCommand);
//                break;
        execMuteVideoFFmpegBinary(complexCommand);
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
//                        String[] complexCommand = {"-ss", "" + mStartPosition / 1000, "-y", "-i", handleFileName, "-t", "" + (mEndPosition - mStartPosition) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", handleFileName};
//                        String[] complexCommand  = {"-i", handleFileName, "-i", recordAudioList.get(0).path, "-filter_complex", "[1]adelay=1000|1000[aud];[0][aud]amix", "-c:v", "copy", handleFileName};
//                        execCutVideoFFmpegBinary(complexCommand);
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
//            String[] complexCommand = {"-ss", "" + mStartPosition / 1000, "-y", "-i", handleFileName, "-t", "" + (mEndPosition - mStartPosition) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", handleFileName};
//            String[] complexCommand  = {"-i", handleFileName, "-i", recordAudioList.get(0).path, "-filter_complex", "[1]adelay=1000|1000[aud];[0][aud]amix", "-c:v", "copy", handleFileName};
//            execCutVideoFFmpegBinary(complexCommand);
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

    private void execAddAudioToVideoFFmpegBinary(final String[] command) {
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

    public static class DownloadFileFromURL extends AsyncTask<String, String, String> {
        String imageURL;

        public DownloadFileFromURL(String imageURL){
            this.imageURL = imageURL;
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(imageURL);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/share_content.mp4");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
//            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
//            dismissDialog(progress_bar_type);

        }

    }
}
