package com.annazou.notebook;

import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class MoveableView implements View.OnTouchListener {
    private static final long LONG_PRESS_TIME = 300;
    private static final float LONG_PRESS_ELEVALTION = 10;

    View mView;
    boolean isLongPress;

    static MoveableView addMoveControl(View view){
        return  new MoveableView(view);
    }

    public MoveableView(View view){
        mView = view;
        view.setOnTouchListener(this);
    }

    float lastX;
    float lastY;

    int maxX;
    int maxY;
    ViewGroup.MarginLayoutParams mLayoutParams;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isLongPress = false;
                lastX = event.getX();
                lastY = event.getY();
                mLayoutParams = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
                maxX = mView.getRootView().getMeasuredWidth() - mView.getMeasuredWidth();
                maxY = mView.getRootView().getMeasuredHeight() - mView.getMeasuredHeight();
                break;
            case MotionEvent.ACTION_MOVE:
            float x = event.getX();
            float y = event.getY();
            if (!isLongPress && (event.getEventTime() - event.getDownTime()) >= LONG_PRESS_TIME
                    && Math.abs(x - lastX) < 20 && Math.abs(y - lastY) < 20) {
                isLongPress = true;
                float elevation = mView.getElevation();
                mView.setElevation(elevation + LONG_PRESS_ELEVALTION);
                mView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            if(isLongPress){
                mLayoutParams.leftMargin += x - lastX;
                mLayoutParams.leftMargin = Math.min(maxX,mLayoutParams.leftMargin);

                mLayoutParams.topMargin += y - lastY;
                mLayoutParams.topMargin = Math.min(maxY,mLayoutParams.topMargin);

                mLayoutParams.rightMargin -= x - lastX;
                mLayoutParams.rightMargin = Math.min(maxX, mLayoutParams.rightMargin);

                mLayoutParams.bottomMargin -= y - lastY;
                mLayoutParams.bottomMargin = Math.min(maxY,mLayoutParams.bottomMargin);

                mView.setLayoutParams(mLayoutParams);

            }
            break;

            case MotionEvent.ACTION_UP:
                if(isLongPress){
                    float elevation = mView.getElevation();
                    mView.setElevation(elevation - LONG_PRESS_ELEVALTION);
                }
        }
        return false;
    }
}
