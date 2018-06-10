package com.shou.john.mimicvideo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by john on 2018/4/2.
 */

public class SearchItemDecoration extends RecyclerView.ItemDecoration {
    private Paint paintBlue, paintRed, paintBorder;
    private int offset;
    private int headerCount;

    Bitmap bitmap;
    int bitmap_w, bitmap_h;
    Rect rectSrc;

    public SearchItemDecoration(Context c, int headerCount){
        offset = 5;
        paintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBlue.setColor(Color.GRAY);
        paintBlue.setStyle(Paint.Style.STROKE);
        paintBlue.setStrokeWidth(5);

        paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRed.setColor(Color.RED);
        paintRed.setStyle(Paint.Style.STROKE);
        paintRed.setStrokeWidth(1);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setColor(Color.GREEN);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(10);

        this.headerCount = headerCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

        int position = params.getViewPosition();
        if(position < headerCount){
            return;
        }

        outRect.set(offset, offset, offset, offset);
    }
}
