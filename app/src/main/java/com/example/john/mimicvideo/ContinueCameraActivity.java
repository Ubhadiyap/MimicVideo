package com.example.john.mimicvideo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.john.mimicvideo.utils.ApplicationParameter;
import com.example.john.mimicvideo.view.RecordedButton;
import com.example.john.mimicvideo.view.videoRecorder.client.RecorderClient;
import com.example.john.mimicvideo.view.videoRecorder.core.listener.IVideoChange;
import com.example.john.mimicvideo.view.videoRecorder.filter.softaudiofilter.SetVolumeAudioFilter;
import com.example.john.mimicvideo.view.videoRecorder.model.MediaConfig;
import com.example.john.mimicvideo.view.videoRecorder.model.RecordConfig;
import com.example.john.mimicvideo.view.videoRecorder.model.Size;
import com.example.john.mimicvideo.view.videoRecorder.tools.TimeHandler;
import com.example.john.mimicvideo.view.videoRecorder.tools.VideoSplicer;
import com.example.john.mimicvideo.view.videoRecorder.ui.AspectTextureView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by john on 2018/5/21.
 */

public class ContinueCameraActivity extends BaseActivity implements
        TextureView.SurfaceTextureListener, View.OnClickListener, IVideoChange {

    //錄製影片
    private static final int HANDLER_RECORD = 200;

    private static final String TAG = "RecordingActivity2";
    public static final String IS_SQUARE = "is_square";
    private static final int MIN_RECORD_DURATION = 10000;//10S
    private static final int MAX_RECORD_DURATION = 30000;//30S
    protected RecorderClient mRecorderClient;
    protected AspectTextureView mTextureView;
    protected Handler mainHander;
    private ProgressBar mTimeProgressBar;
    private TextView mTimeView;
    protected String mSaveVideoPath = null;
    protected boolean mIsSquare = false;
    RecordConfig recordConfig;

    //新版頁面按鈕
    private ImageView iv_back;
    private ImageView iv_finish;
    private RecordedButton rb_start;
    private RelativeLayout rl_bottom;
    private ImageView iv_change_flash;
    private ImageView iv_change_camera;

    private static Rect getScreenBounds(Context context) {
        if (context == null) {
            return null;
        }
        Rect rect = new Rect();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        rect.set(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        return rect;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        mIsSquare = i.getBooleanExtra(IS_SQUARE, false);
//        mSaveVideoPath = Environment.getExternalStorageDirectory().getPath() + "/live_save_video" + System.currentTimeMillis() + ".mp4";
        mSaveVideoPath = ApplicationParameter.FILE_SAVE_PATH;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        mTextureView = (AspectTextureView) findViewById(R.id.preview_textureview);
        mTextureView.setKeepScreenOn(true);
        mTextureView.setSurfaceTextureListener(this);

        mTimeProgressBar = (ProgressBar) findViewById(R.id.pb_timeline);
        mTimeProgressBar.setMax(MAX_RECORD_DURATION);
        mTimeProgressBar.setProgress(0);
        mTimeView = (TextView) findViewById(R.id.timeview);

        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    return ;
                }
                fallBack();
            }
        });

        iv_finish = findViewById(R.id.iv_finish);
        iv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mergeFile();
            }
        });

        iv_change_flash = (ImageView) findViewById(R.id.iv_change_flash);
        iv_change_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecorderClient.toggleFlashLight();
                if(mRecorderClient.toggleFlashLight()){
                    iv_change_flash.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_flash_open);
                }else{
                    iv_change_flash.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_flash_close);
                }
            }
        });

        iv_change_camera = (ImageView) findViewById(R.id.iv_change_camera);
        iv_change_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecorderClient.swapCamera();
                iv_change_flash.setVisibility(mRecorderClient.isFrontCamera() ? View.GONE : View.VISIBLE);
            }
        });

        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        rb_start = (RecordedButton) findViewById(R.id.rb_start);
        rb_start.setMax(MAX_RECORD_DURATION);

        rb_start.setOnGestureListener(new RecordedButton.OnGestureListener() {
            @Override
            public void onLongClick() {
                //長按錄影
//                isRecordedOver = false;
//                mMediaRecorder.startRecord();
                rb_start.setSplit();
//                myHandler.sendEmptyMessageDelayed(HANDLER_RECORD, 50);
//                cameraTypeList.add(mMediaRecorder.getCameraType());
//
//                isVideoData = true;

                resumeRecording();
                System.out.println("onLongClick");
            }
            @Override
            public void onClick() {
//                if(!isVideoData) {
//                    //點擊拍照
//                    dialogTextView = showProgressDialog();
//                    dialogTextView.setText("讀取螢幕, 請保持静止");
//                    mMediaRecorder.startRecord();
//                    myHandler.sendEmptyMessageDelayed(HANDLER_CAMERA_PHOTO, 30);
//                }
            }
            @Override
            public void onLift() {
//                isRecordedOver = true;
//                mMediaRecorder.stopRecord();
                pauseRecording();
                changeButton(mp4List.size() > 0);
                System.out.println("onLift");
            }
            @Override
            public void onOver() {
//                isRecordedOver = true;
                //rb_start.closeButton();
//                mMediaRecorder.stopRecord();
//                videoFinish();

                pauseRecording();
                System.out.println("onOver");
            }
        });

        Rect rect = getScreenBounds(getApplicationContext());
        int sWidth = rect.width();
        ((RelativeLayout.LayoutParams)(findViewById(R.id.divide_view)).getLayoutParams()).leftMargin =
                sWidth * MIN_RECORD_DURATION / MAX_RECORD_DURATION;

        findViewById(R.id.btn_swap).setOnClickListener(this);
        findViewById(R.id.btn_flash).setOnClickListener(this);
        findViewById(R.id.btn_del).setOnClickListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cap).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){//按下
                    resumeRecording();
                }else if(event.getAction()==MotionEvent.ACTION_UP){//抬起
                    pauseRecording();
                }
                return false;
            }
        });
        findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);

        prepareStreamingClient();
//        onSetFilters();

        mTimeHandle = new TimeHandler(Looper.getMainLooper(), mTimeTask);

        mp4List = new ArrayList<>();
        durationList = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        hideProgressDialog();
        if (mainHander != null) {
            mainHander.removeCallbacksAndMessages(null);
        }
        if (isRecording) {
            mRecorderClient.stopRecording();
        }
        if (mRecorderClient != null) {
            mRecorderClient.destroy();
        }
        super.onDestroy();
    }

    private void prepareStreamingClient() {
        mRecorderClient = new RecorderClient();

        recordConfig = RecordConfig.obtain();
        if (mIsSquare) {
            recordConfig.setTargetVideoSize(new Size(480, 480));
        } else {
            recordConfig.setTargetVideoSize(new Size(640, 480));
        }
        recordConfig.setSquare(true);
        recordConfig.setBitRate(750 * 1024);
        recordConfig.setVideoFPS(20);
        recordConfig.setVideoGOP(1);
        recordConfig.setRenderingMode(MediaConfig.Rending_Model_OpenGLES);
        //camera
        recordConfig.setDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        int frontDirection, backDirection;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        frontDirection = cameraInfo.orientation;
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        backDirection = cameraInfo.orientation;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recordConfig.setFrontCameraDirectionMode((frontDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90) | MediaConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
            recordConfig.setBackCameraDirectionMode((backDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270));
        } else {
            recordConfig.setBackCameraDirectionMode((backDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180));
            recordConfig.setFrontCameraDirectionMode((frontDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0) | MediaConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
        }
        //save video
        recordConfig.setSaveVideoPath(mSaveVideoPath);

        if (!mRecorderClient.prepare(this, recordConfig)) {
            mRecorderClient = null;
            Log.e("RecordingActivity", "prepare,failed!!");
            Toast.makeText(this, "StreamingClient prepare failed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //resize textureview
        Size s = mRecorderClient.getVideoSize();
        mTextureView.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) s.getWidth()) / s.getHeight());

        mRecorderClient.setVideoChangeListener(this);

        mRecorderClient.setSoftAudioFilter(new SetVolumeAudioFilter());
    }

//    protected void onSetFilters() {
//        ArrayList<DrawMultiImageFilter.ImageDrawData> infos = new ArrayList<>();
//        DrawMultiImageFilter.ImageDrawData data = new DrawMultiImageFilter.ImageDrawData();
//        data.resId = R.drawable.t;
//        data.rect = new Rect(100, 100, 238, 151);
//        infos.add(data);
//        mRecorderClient.setHardVideoFilter(new DrawMultiImageFilter(this, infos));
//    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        mTextureView.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) width) / height);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mRecorderClient != null) {
            mRecorderClient.startPreview(surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mRecorderClient != null) {
            mRecorderClient.updatePreview(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mRecorderClient != null) {
            mRecorderClient.stopPreview(true);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private String getTmpFolderPath() {
        File dic = new File(Environment.getExternalStorageDirectory().getPath() + "/tmpVideoRecord/");
        if(!dic.exists()){
            dic.mkdirs();
        }
        return dic.toString();
    }

    private void resumeRecording() {
        if (totalTime >= MAX_RECORD_DURATION) {
            Log.i(TAG, "already full");
            return ;
        }
        mFileIdx++;
        mRecorderClient.updatePath(getTmpFolderPath() + "/"+mFileIdx+".mp4");
        mRecorderClient.startRecording();
        isRecording = true;
        mStartTime = System.currentTimeMillis();
        mTimeHandle.sendLoopMsg(0L, 100L);
    }
    private void pauseRecording() {
        if (!isRecording) {
            return ;
        }
        mRecorderClient.stopRecording();
        isRecording = false;
        stopTimeTask();

        String path = mRecorderClient.getFilePath();
//        if (mCurrentDuration > 1000) {
//            if (!TextUtils.isEmpty(path)) {
//                totalTime += mCurrentDuration;
//                durationList.add(mCurrentDuration);
//                mp4List.add(path);
//                findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);
//                return ;
//            }
//        }
        if (!TextUtils.isEmpty(path)) {
            totalTime += mCurrentDuration;
            durationList.add(mCurrentDuration);
            mp4List.add(path);
            findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);
            return ;
        }
//        if (!TextUtils.isEmpty(path)) {
//            File file = new File(path);
//            if (file.exists()) {
//                file.delete();
//            }
//        }
//        mFileIdx--;
//        mTimeProgressBar.setProgress((int) (totalTime));
//        mTimeView.setText((totalTime)/1000+"s");
//
//        //刪除過短影片
//        rb_start.setProgress((int) (totalTime));
//        rb_start.deleteSplit();
//
//        if (totalTime < MIN_RECORD_DURATION) {
//            findViewById(R.id.btn_ok).setEnabled(false);
//        }
//        findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);
//        Toast.makeText(this, "錄製影片太短", Toast.LENGTH_SHORT).show();
    }

    private boolean isRecording = false;
    private int mFileIdx = 0;
    private ArrayList<String> mp4List;
    private ArrayList<Long> durationList;

    private void mergeFile() {
        finishRecording(mSaveVideoPath);
    }

    public void finishRecording(String filePath) {
        mFileIdx = 0;
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
        if(mp4List.size() == 1) {
            new File(mp4List.get(0)).renameTo(new File(mSaveVideoPath));
            //Toast.makeText(getApplicationContext(), "视频文件已保存至"+ mSaveVideoPath, Toast.LENGTH_SHORT).show();
            mp4List.clear();
            durationList.clear();
            rb_start.cleanSplit();
            Intent intent = new Intent();
            intent.setClass(ContinueCameraActivity.this, VideoPreviewActivity.class);
            startActivity(intent);
            return;
        }

        new Mp4MergeTask(mp4List, mSaveVideoPath, new IMergeListener() {
            @Override
            public void onMergeBegin(boolean success) {
                showProgressDialog2();
            }

            @Override
            public void onMergeEnd(boolean success) {
                hideProgressDialog();
//                if (success) {
//                    Toast.makeText(getApplicationContext(), "视频文件已保存至" + mSaveVideoPath, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "视频文件保存失败!!!", Toast.LENGTH_SHORT).show();
//                }
                //below delete record
               mp4List.clear();
               durationList.clear();
                rb_start.cleanSplit();
                Intent intent = new Intent();
                intent.setClass(ContinueCameraActivity.this, VideoPreviewActivity.class);
                startActivity(intent);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Dialog mDialog;

    private void showProgressDialog2() {
        Dialog dialog = new Dialog(ContinueCameraActivity.this);
        dialog.setContentView(new ProgressBar(ContinueCameraActivity.this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("合成影片中...");
        mDialog = dialog;
        dialog.show();
    }
    private void hideProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void fallBack() {
//        if(mFileIdx > 0) {
//            mFileIdx--;
//        }
//        if(mp4List.size()>0) {
//            String path = mp4List.remove(mp4List.size() - 1);
//            File file = new File(path);
//            if(file.exists()){
//                file.delete();
//            }
//            Long time = durationList.remove(durationList.size() - 1);
//            totalTime -= time.longValue();
//            mTimeProgressBar.setProgress((int) (totalTime));
//            mTimeView.setText((totalTime)/1000+"s");
//            if (totalTime < MIN_RECORD_DURATION) {
//                findViewById(R.id.btn_ok).setEnabled(false);
//            }
//            findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);
//        }

        if (rb_start.isDeleteMode()) {//判断是否要删除视频段落
            if(mFileIdx > 0) {
                mFileIdx--;
            }
            if(mp4List.size()>0) {
                String path = mp4List.remove(mp4List.size() - 1);
                File file = new File(path);
                if(file.exists()){
                    file.delete();
                }
                Long time = durationList.remove(durationList.size() - 1);
                totalTime -= time.longValue();
                mTimeProgressBar.setProgress((int) (totalTime));
                mTimeView.setText((totalTime)/1000+"s");
//                if (totalTime < MIN_RECORD_DURATION) {
//                    findViewById(R.id.btn_ok).setEnabled(false);
//                }
//                findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);

                rb_start.setProgress((int) (totalTime));
                rb_start.deleteSplit();
                iv_back.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_delete_green);

                int size = mp4List.size();
                if(size > 0){
                    changeButton(true);
                }else{
                    changeButton(false);
                }
            }

        } else if (mp4List.size()>0) {
            rb_start.setDeleteMode(true);
            iv_back.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_delete_red);
        }
    }

    private void changeButton(boolean flag){

        if(flag){
            rl_bottom.setVisibility(View.VISIBLE);
        }else{
            rl_bottom.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_del:
                if (isRecording) {
                    return ;
                }
                fallBack();
                break;
            case R.id.btn_ok:
                if (isRecording) {
                    return ;
                }
                mergeFile();
                break;
            case R.id.btn_swap:
                mRecorderClient.swapCamera();
                findViewById(R.id.btn_flash).setVisibility(mRecorderClient.isFrontCamera() ? View.GONE : View.VISIBLE);
                break;
            case R.id.btn_flash:
                mRecorderClient.toggleFlashLight();
                break;
        }
    }

    private TimeHandler mTimeHandle;
    private long mCurrentDuration = 0;//ms
    private long totalTime = 0;
    private long mStartTime = 0;//ms
    private TimeHandler.Task mTimeTask = new TimeHandler.Task() {
        @Override
        public void run() {
            long timeLapse = System.currentTimeMillis() - mStartTime;
            mCurrentDuration = timeLapse;
            Log.i(TAG, "duration="+mCurrentDuration);

            rb_start.setProgress((int) (totalTime + timeLapse));

            mTimeProgressBar.setProgress((int) (totalTime + timeLapse));
            mTimeView.setText((totalTime + timeLapse)/1000+"s");
            if ((totalTime + timeLapse) >= MAX_RECORD_DURATION) {
                pauseRecording();
            } else if ((totalTime + timeLapse) >= MIN_RECORD_DURATION) {
                findViewById(R.id.btn_ok).setEnabled(true);
            } else {
                findViewById(R.id.btn_ok).setEnabled(false);
            }
        }
    };
    private void stopTimeTask() {
        mTimeHandle.clearMsg();
    }

    private static class Mp4MergeTask extends AsyncTask<Object, Object, Boolean> {

        private ArrayList<String> list;
        private String path;
        private IMergeListener listener;
        public Mp4MergeTask(ArrayList<String> uriList, String outputPath, IMergeListener listener) {
            list = uriList;
            path = outputPath;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (listener != null) {
                listener.onMergeBegin(true);
            }
        }

        @Override
        protected Boolean doInBackground(Object... objects) {
            boolean ret = new VideoSplicer(list, path).joinVideo();

            return ret;
        }

        @Override
        protected void onPostExecute(Boolean o) {
            super.onPostExecute(o);
            if (listener != null) {
                listener.onMergeEnd(o);
            }
        }
    }
    private static interface IMergeListener {
        void onMergeBegin(boolean success);
        void onMergeEnd(boolean success);
    }
}
