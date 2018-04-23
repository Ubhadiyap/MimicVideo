package com.example.john.mimicvideo.utils;

import android.content.Context;

import com.example.john.mimicvideo.R;

/**
 * Created by john on 2018/4/15.
 */

public class MyProgressDialog {
    private static Context mContext ;
    private static MyProgressDialog myProgressDialog;

    public static MyProgressDialog getInstance(Context context){
        mContext = context;

        return myProgressDialog;
    }

    public static void show(){

    }

    public static void dismiss(){

    }



}
