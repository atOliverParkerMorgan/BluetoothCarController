package com.example.bluetoothcarcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.bluetoothcontroler.R;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

public class AutopilotCanvasView extends View {
    private final Drawable mCarIcon;
    private final Drawable mPointIcon;

    private final long ALLOWED_SCROLL_X = 900;
    private final long ALLOWED_SCROLL_Y = 900;
    private long currentScrollX = ALLOWED_SCROLL_X/2;
    private long currentScrollY = ALLOWED_SCROLL_Y/2;

    public float mPosX;
    public float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;

    private final List<float[]> points = new LinkedList<>();

    private static final int INVALID_POINTER_ID = -1;

    private final ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    public AutopilotCanvasView(Context context) {
        this(context, null, 0);
    }

    public AutopilotCanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public AutopilotCanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCarIcon = getContext().getDrawable(R.drawable.ic_baseline_drive_eta_24);
        mCarIcon.setBounds(0, 0, mCarIcon.getIntrinsicWidth(), mCarIcon.getIntrinsicHeight());

        mPointIcon = getContext().getDrawable(R.drawable.ic_baseline_circle_24);
        mPointIcon.setBounds(0, 0, mPointIcon.getIntrinsicWidth()/2, mPointIcon.getIntrinsicHeight()/2);

        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        // 100 random points
//        for (int i = 0; i < 100 ; i++) {
//            float x = (float) (Math.random() * 1000);
//            float y = (float) (Math.random() * 1000);
//
//            float[] cords = new float[]{x, y};
//            addPoint(cords);
//        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;


                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                Log.d("X", String.valueOf(x));
                Log.d("Y", String.valueOf(y));


                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    if(currentScrollX<0 && dx < 0) break;
                    if(currentScrollX>ALLOWED_SCROLL_X && dx > 0) break;
                    if(currentScrollY<0 && dy < 0) break;
                    if(currentScrollY>ALLOWED_SCROLL_Y && dy > 0) break;


                    currentScrollX += dx;
                    currentScrollY += dy;

                    mPosX += dx;
                    mPosY += dy;
                    invalidate();

                   for (float[]cords : points) {
                        cords[0] += dx;
                        cords[1] += dy;
                        invalidate();
                    }


                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);

        canvas.translate(mPosX, mPosY);
        mCarIcon.draw(canvas);
        canvas.restore();

   ;
        try {
            for (float[] cord : points) {
                canvas.save();
                canvas.scale(mScaleFactor, mScaleFactor);
                canvas.translate(cord[0], cord[1]);
                mPointIcon.draw(canvas);
                canvas.restore();
            }
        }
        catch (ConcurrentModificationException ignored){}

    }



    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.0f));

            invalidate();
            return true;
        }
    }

    public void addPoint(float[] cords){
        points.add(cords);
        invalidate();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mPosX = (float) w/2;
        this.mPosY = (float) h/2;
        super.onSizeChanged(w, h, oldw, oldh);
    }
}