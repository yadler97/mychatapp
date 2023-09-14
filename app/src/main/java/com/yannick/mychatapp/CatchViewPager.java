package com.yannick.mychatapp;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class CatchViewPager extends ViewPager {
    float initialPositionY;
    private Context context;

    public CatchViewPager(Context context) {
        super(context);
    }

    public CatchViewPager(Context context, AttributeSet as) {
        super(context, as);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (detectSwipeToRight(event)) {
            try {
                return super.onInterceptTouchEvent(event);
            } catch (IllegalArgumentException e) {
                Log.e("IllegalArgumentException", e.toString());
            }
        }

        return false;
    }

    public boolean detectSwipeToRight(MotionEvent event){
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initialPositionY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float currentY = event.getY();
                if (initialPositionY - currentY > 500 || initialPositionY - currentY < -500) {
                    Intent intent = new Intent("closefullscreen");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    return false;
                }
        }

        return true;
    }
}
