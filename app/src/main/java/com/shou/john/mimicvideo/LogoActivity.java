package com.shou.john.mimicvideo;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;

import com.example.john.mimicvideo.R;

public class LogoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Intent intent = new Intent();
                intent.setClass(LogoActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        }.start();

    }
}
