package com.yannick.mychatapp;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CatchViewPager extends ViewPager {

    public CatchViewPager(Context context) {
        super(context);
    }

    public CatchViewPager(Context context, AttributeSet as) {
        super(context, as);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
