package com.shou.john.mimicvideo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.john.mimicvideo.R;

/**
 * Created by john on 2018/3/25.
 */

public class BaseActivity extends AppCompatActivity {

    private AlertDialog progressDialog;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
    }

    //hide the keyboard
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public TextView showProgressDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        View view = View.inflate(this, com.zhaoshuang.weixinrecorded.R.layout.dialog_loading, null);
        builder.setView(view);
        ProgressBar pb_loading = (ProgressBar) view.findViewById(com.zhaoshuang.weixinrecorded.R.id.pb_loading);
        TextView tv_hint = (TextView) view.findViewById(com.zhaoshuang.weixinrecorded.R.id.tv_hint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pb_loading.setIndeterminateTintList(ContextCompat.getColorStateList(this, com.zhaoshuang.weixinrecorded.R.color.dialog_pro_color));
        }
        tv_hint.setText("影片處理中");
        progressDialog = builder.create();
        progressDialog.show();

        return tv_hint;
    }

    public void closeProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Dialog showLoadDialog(){
        dialog = new Dialog(this, R.style.selectorDialog);
        dialog.setContentView(R.layout.activity_loading);

        // 由程式設定 Dialog 視窗外的明暗程度, 亮度從 0f 到 1f
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.5f;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
        return dialog;
    }

    public void closeLoadDialog(){
        if(dialog != null){
            dialog.dismiss();
        }
    }
}
