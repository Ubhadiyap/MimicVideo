package com.example.john.mimicvideo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.yixia.camera.MediaRecorderNative;
import com.yixia.camera.VCamera;
import com.yixia.camera.model.MediaObject;
import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.zhaoshuang.weixinrecorded.FocusSurfaceView;
import com.zhaoshuang.weixinrecorded.MyVideoView;
import com.zhaoshuang.weixinrecorded.RecordedButton;
import com.zhaoshuang.weixinrecorded.SDKUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FantasticCameraActivity extends BaseActivity {
    private static final int REQUEST_KEY = 100;
    //录制视频
    private static final int HANDLER_RECORD = 200;
    //编辑视频
    private static final int HANDLER_EDIT_VIDEO = 201;
    //拍摄照片
    private static final int HANDLER_CAMERA_PHOTO = 202;

    //返回重新拍攝
    private int BACK_CAMERA = 300;

    private MediaRecorderNative mMediaRecorder;
    private MediaObject mMediaObject;
    private FocusSurfaceView sv_ffmpeg;
    private RecordedButton rb_start;
    private RelativeLayout rl_bottom;
    private RelativeLayout rl_bottom2;
    private ImageView iv_back;
    private TextView tv_hint;
    private TextView dialogTextView;
    private MyVideoView vv_play;
    private ImageView iv_photo;
    private RelativeLayout rl_top;
    private ImageView iv_finish;
    private ImageView iv_next;
    private ImageView iv_close;
    private ImageView iv_change_camera;
    private ImageView backImg;

    private Dialog loadDialog;

    //最大录制时间
    private int maxDuration = 30000;
    //本次段落是否录制完成
    private boolean isRecordedOver;
    private ImageView iv_change_flash;
    private List<Integer> cameraTypeList = new ArrayList<>();

    //是否视频数据
    private boolean isVideoData;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(com.zhaoshuang.weixinrecorded.R.layout.activity_recorded);

        SDKUtil.initSDK(this);

        initUI();
        initData();

        initMediaRecorder();
    }

    private void initUI() {

        sv_ffmpeg = (FocusSurfaceView) findViewById(com.zhaoshuang.weixinrecorded.R.id.sv_ffmpeg);
        rb_start = (RecordedButton) findViewById(com.zhaoshuang.weixinrecorded.R.id.rb_start);
        vv_play = (MyVideoView) findViewById(com.zhaoshuang.weixinrecorded.R.id.vv_play);
        iv_finish = (ImageView) findViewById(com.zhaoshuang.weixinrecorded.R.id.iv_finish);
        iv_back = (ImageView) findViewById(com.zhaoshuang.weixinrecorded.R.id.iv_back);
        tv_hint = (TextView) findViewById(com.zhaoshuang.weixinrecorded.R.id.tv_hint);
        rl_bottom = (RelativeLayout) findViewById(com.zhaoshuang.weixinrecorded.R.id.rl_bottom);
        rl_bottom2 = (RelativeLayout) findViewById(com.zhaoshuang.weixinrecorded.R.id.rl_bottom2);
        iv_next = (ImageView) findViewById(com.zhaoshuang.weixinrecorded.R.id.iv_next);
        iv_close = (ImageView) findViewById(com.zhaoshuang.weixinrecorded.R.id.iv_close);
        iv_change_flash = (ImageView) findViewById(com.zhaoshuang.weixinrecorded.R.id.iv_change_flash);
        iv_change_camera = (ImageView) findViewById(com.zhaoshuang.weixinrecorded.R.id.iv_change_camera);
        iv_photo = (ImageView) findViewById(com.zhaoshuang.weixinrecorded.R.id.iv_photo);
        rl_top = (RelativeLayout) findViewById(com.zhaoshuang.weixinrecorded.R.id.rl_top);
        backImg = findViewById(R.id.backImg);
    }

    private void initData() {

        sv_ffmpeg.setTouchFocus(mMediaRecorder);

        rb_start.setMax(maxDuration);

        rb_start.setOnGestureListener(new RecordedButton.OnGestureListener() {
            @Override
            public void onLongClick() {
                //長按錄影
                isRecordedOver = false;
                mMediaRecorder.startRecord();
                rb_start.setSplit();
                myHandler.sendEmptyMessageDelayed(HANDLER_RECORD, 50);
                cameraTypeList.add(mMediaRecorder.getCameraType());

                isVideoData = true;
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
                isRecordedOver = true;
                mMediaRecorder.stopRecord();
                changeButton(mMediaObject.getMediaParts().size() > 0);
            }
            @Override
            public void onOver() {
                isRecordedOver = true;
                rb_start.closeButton();
                mMediaRecorder.stopRecord();
                videoFinish();
            }
        });

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rb_start.isDeleteMode()) {//判断是否要删除视频段落
                    MediaObject.MediaPart lastPart = mMediaObject.getPart(mMediaObject.getMediaParts().size() - 1);
                    mMediaObject.removePart(lastPart, true);
                    rb_start.setProgress(mMediaObject.getDuration());
                    rb_start.deleteSplit();
                    if (cameraTypeList.size() > 0) {
                        cameraTypeList.remove(cameraTypeList.size() - 1);
                    }
                    iv_back.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_delete_green);

                    int size = mMediaObject.getMediaParts().size();
                    if(size > 0){
                        changeButton(true);
                    }else{
                        isVideoData = false;
                        changeButton(false);
                    }
                } else if (mMediaObject.getMediaParts().size() > 0) {
                    rb_start.setDeleteMode(true);
                    iv_back.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_delete_red);
                }
            }
        });

        iv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoFinish();
            }
        });

        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoData) {
                    rb_start.setDeleteMode(false);
                    Intent intent = new Intent(FantasticCameraActivity.this, VideoPreviewActivity.class);
                    intent.putExtra("path", SDKUtil.VIDEO_PATH + "/finish.mp4");
                    startActivityForResult(intent, REQUEST_KEY);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("imagePath", imagePath);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMediaRecorderState();
            }
        });

        iv_change_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaRecorder.changeFlash(FantasticCameraActivity.this)) {
                    iv_change_flash.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_flash_open);
                } else {
                    iv_change_flash.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_flash_close);
                }
            }
        });

        iv_change_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaRecorder.switchCamera();
                iv_change_flash.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_flash_close);
            }
        });
    }

    private void changeButton(boolean flag){

        if(flag){
            tv_hint.setVisibility(View.VISIBLE);
            rl_bottom.setVisibility(View.VISIBLE);
        }else{
            tv_hint.setVisibility(View.GONE);
            rl_bottom.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化視頻拍攝狀態
     */
    private void initMediaRecorderState(){

        isVideoData = false;
        vv_play.setVisibility(View.GONE);
        vv_play.pause();
        iv_photo.setVisibility(View.GONE);

        rl_top.setVisibility(View.VISIBLE);
        rb_start.setVisibility(View.VISIBLE);
        rl_bottom2.setVisibility(View.GONE);
        changeButton(false);
        tv_hint.setVisibility(View.VISIBLE);

        LinkedList<MediaObject.MediaPart> list = new LinkedList<>();
        list.addAll(mMediaObject.getMediaParts());

        for (MediaObject.MediaPart part : list){
            mMediaObject.removePart(part, true);
        }

        rb_start.setProgress(mMediaObject.getDuration());
        rb_start.cleanSplit();
    }

    private void videoFinish() {
        if(mMediaObject.getDuration() > 2000){
            changeButton(false);
            rb_start.setVisibility(View.GONE);

//        dialogTextView = showProgressDialog();
            loadDialog = showLoadDialog();

            myHandler.sendEmptyMessage(HANDLER_EDIT_VIDEO);
        }else{
            Toast.makeText(this, "最少限制2秒", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_RECORD: {
                    //拍摄视频的handler
                    if (!isRecordedOver) {
                        if (rl_bottom.getVisibility() == View.VISIBLE) {
                            changeButton(false);
                        }
                        rb_start.setProgress(mMediaObject.getDuration());
                        myHandler.sendEmptyMessageDelayed(HANDLER_RECORD, 30);
                    }
                }
                break;
                case HANDLER_EDIT_VIDEO: {
                    //合成视频的handler
                    int progress = UtilityAdapter.FilterParserAction("", UtilityAdapter.PARSERACTION_PROGRESS);
//                    if (dialogTextView != null) dialogTextView.setText("视频编译中 " + progress + "%");
                    if(loadDialog != null){
                        TextView loadingTitleTxt = loadDialog.findViewById(R.id.loadingTitleTxt);
                        loadingTitleTxt.setText("影片合成中");
                        NumberProgressBar number_progress_bar = loadDialog.findViewById(R.id.number_progress_bar);
                        number_progress_bar.setProgress(progress);
                    }
                    if (progress == 100) {
                        syntVideo();
                    } else if (progress == -1) {
                        //closeProgressDialog();
                        closeLoadDialog();
                        Toast.makeText(getApplicationContext(), "影片合成失敗", Toast.LENGTH_SHORT).show();
                    } else {
                        sendEmptyMessageDelayed(HANDLER_EDIT_VIDEO, 20);
                    }
                }
                break;
                case HANDLER_CAMERA_PHOTO: {
                    //拍照
                    if(mMediaRecorder.getRecordState()){
                        mMediaRecorder.stopRecord();
                    }
                    int progress = UtilityAdapter.FilterParserAction("", UtilityAdapter.PARSERACTION_PROGRESS);
                    if (progress == 100) {
                        syntCamera();
                    } else if (progress == -1) {
                        //closeProgressDialog();
                        closeLoadDialog();
                        Toast.makeText(getApplicationContext(), "照片拍攝失败", Toast.LENGTH_SHORT).show();
                    } else {
//                        dialogTextView.setText("照片编辑中");
                        ((TextView)loadDialog.findViewById(R.id.loadingTitleTxt)).setText("照片合成中");
                        sendEmptyMessageDelayed(HANDLER_CAMERA_PHOTO, 10);
                    }
                }
                break;
            }
        }
    };

    /**
     * 合成视频
     */
    @SuppressLint("StaticFieldLeak")
    private void syntVideo(){

        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
//                if(dialogTextView != null) dialogTextView.setText("视频合成中");
                if(loadDialog != null){
                    ((TextView)loadDialog.findViewById(R.id.loadingTitleTxt)).setText("影片合成中");
                }
            }
            @Override
            protected String doInBackground(Void... params) {

                List<String> pathList = new ArrayList<>();
                for (int x = 0; x < mMediaObject.getMediaParts().size(); x++) {
                    MediaObject.MediaPart mediaPart = mMediaObject.getMediaParts().get(x);

                    String mp4Path = SDKUtil.VIDEO_PATH+"/"+x+".mp4";
                    List<String> list = new ArrayList<>();
                    list.add(mediaPart.mediaPath);
                    ts2Mp4(list, mp4Path);
                    pathList.add(mp4Path);
                }

                List<String> tsList = new ArrayList<>();
                for (int x = 0; x < pathList.size(); x++) {
                    String path = pathList.get(x);
                    String ts = SDKUtil.VIDEO_PATH+"/"+x+".ts";
                    mp4ToTs(path, ts);
                    tsList.add(ts);
                }

                String output = SDKUtil.VIDEO_PATH+"/share_content.mp4";
                boolean flag = ts2Mp4(tsList, output);
                if(!flag) output = "";
                deleteDirRoom(new File(SDKUtil.VIDEO_PATH), output);
                return output;
            }
            @Override
            protected void onPostExecute(String result) {
//                closeProgressDialog();
                closeLoadDialog();

                if(!TextUtils.isEmpty(result)){
//                    rl_bottom2.setVisibility(View.VISIBLE);
//                    vv_play.setVisibility(View.VISIBLE);
//                    rl_top.setVisibility(View.GONE);
//
//                    vv_play.setVideoPath(result);
//                    vv_play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//                            mp.setLooping(true);
//                            vv_play.start();
//                        }
//                    });
//                    if(vv_play.isPrepared()){
//                        vv_play.setLooping(true);
//                        vv_play.start();
//                    }
                    Intent intent = new Intent();
                    intent.setClass(FantasticCameraActivity.this, VideoPreviewActivity.class);
                    startActivityForResult(intent, BACK_CAMERA);
                    mMediaRecorder.release();
                }else{
                    Toast.makeText(getApplicationContext(), "视频合成失败", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /**
     * 合成照片
     */
    @SuppressLint("StaticFieldLeak")
    private void syntCamera(){

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {

            }
            @Override
            protected Bitmap doInBackground(Void... params) {

                LinkedList<MediaObject.MediaPart> mediaParts = mMediaObject.getMediaParts();
                String tsPath = mediaParts.getFirst().mediaPath;
                ArrayList<String> tsList = new ArrayList<>();
                tsList.add(tsPath);

                String mp4Path = SDKUtil.VIDEO_PATH+"/cameraTemp.mp4";
                ts2Mp4(tsList, mp4Path);

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(mp4Path);
                Bitmap photo = mmr.getFrameAtTime(1);
                mmr.release();

                imagePath = SDKUtil.VIDEO_PATH+"/"+System.currentTimeMillis()+".jpg";
                try {
                    FileOutputStream outputStream = new FileOutputStream(imagePath);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                deleteDirRoom(new File(SDKUtil.VIDEO_PATH), imagePath);

                return photo;
            }
            @Override
            protected void onPostExecute(Bitmap result) {
                //closeProgressDialog();
                closeLoadDialog();
                if(result != null){
                    changeButton(false);
                    rl_bottom2.setVisibility(View.VISIBLE);
                    iv_photo.setVisibility(View.VISIBLE);
                    rl_top.setVisibility(View.GONE);
                    rb_start.setVisibility(View.GONE);
                    iv_photo.setImageBitmap(result);

                }else{
                    Toast.makeText(getApplicationContext(), "照片拍摄失败", Toast.LENGTH_SHORT).show();
                    initMediaRecorderState();
                }
            }
        }.execute();
    }

    /**
     * 删除文件夹下所有文件, 只保留一个
     * @param fileName 保留的文件名称
     */
    public static void deleteDirRoom(File dir, String fileName){

        if(dir.exists() && dir.isDirectory()){
            File[] files = dir.listFiles();
            for(File f: files){
                deleteDirRoom(f, fileName);
            }
        }else if(dir.exists()) {
            if (!dir.getAbsolutePath().equals(fileName)){
                dir.delete();
            }
        }
    }

    public void mp4ToTs(String path, String output){

        //./ffmpeg -i 0.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts ts0.ts

        StringBuilder sb = new StringBuilder("ffmpeg");
        sb.append(" -i");
        sb.append(" "+path);
        sb.append(" -c");
        sb.append(" copy");
        sb.append(" -bsf:v");
        sb.append(" h264_mp4toannexb");
        sb.append(" -f");
        sb.append(" mpegts");
        sb.append(" "+output);

        int i = UtilityAdapter.FFmpegRun("", sb.toString());
    }

    public boolean ts2Mp4(List<String> path, String output){

        //ffmpeg -i "concat:ts0.ts|ts1.ts|ts2.ts|ts3.ts" -c copy -bsf:a aac_adtstoasc out2.mp4

        StringBuilder sb = new StringBuilder("ffmpeg");
        sb.append(" -i");
        String concat="concat:";
        for (String part : path){
            concat += part;
            concat += "|";
        }
        concat = concat.substring(0, concat.length()-1);
        sb.append(" "+concat);
        sb.append(" -c");
        sb.append(" copy");
        sb.append(" -bsf:a");
        sb.append(" aac_adtstoasc");
        sb.append(" -y");
        sb.append(" "+output);

        int i = UtilityAdapter.FFmpegRun("", sb.toString());
        return i == 0;
    }

    /**
     * 初始化录制对象
     */
    private void initMediaRecorder() {

        mMediaRecorder = new MediaRecorderNative();
        String key = String.valueOf(System.currentTimeMillis());
        //设置缓存文件夹
        mMediaObject = mMediaRecorder.setOutputDirectory(key, VCamera.getVideoCachePath());
        //设置视频预览源
        mMediaRecorder.setSurfaceHolder(sv_ffmpeg.getHolder());
        //准备
        mMediaRecorder.prepare();
        //滤波器相关
        UtilityAdapter.freeFilterParser();
        UtilityAdapter.initFilterParser();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaRecorder.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaRecorder.stopPreview();
        iv_change_flash.setImageResource(com.zhaoshuang.weixinrecorded.R.mipmap.video_flash_close);
    }

    @Override
    public void onBackPressed() {
        if(rb_start.getSplitCount() == 0) {
            super.onBackPressed();
        }else{
            initMediaRecorderState();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaObject.cleanTheme();
        mMediaRecorder.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_KEY){
                if(data != null){
                    Intent intent = new Intent();
                    intent.putExtra("videoPath", data.getStringExtra("videoPath"));
                    setResult(RESULT_OK, intent);
                    finish();
                }else{
                    initMediaRecorderState();
                }
            }else if(requestCode == BACK_CAMERA){
                initMediaRecorder();
            }
        }
    }
}