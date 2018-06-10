package com.shou.john.mimicvideo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by john on 2018/4/29.
 */

public class DrawView extends View {
    Paint paint = new Paint();
    public int start = 0;
    public int mWidth = 0;
    public int mHeight = 0;
    int mColor = Color.WHITE;

    public DrawView(Context context) {
        super(context);
    }

    public void setSizeColor(int start, int width, int height, int color) {
        this.start = start;
        mWidth = width;
        mHeight = height;
        mColor = color;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setStrokeWidth(0);
        paint.setColor(mColor);
        canvas.drawRect(start, 0, mWidth, mHeight, paint );
    }
}
