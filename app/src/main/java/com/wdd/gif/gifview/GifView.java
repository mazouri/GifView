package com.wdd.gif.gifview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.wdd.gif.R;

/**
 * Created by wangdd on 15-8-5.
 */
public class GifView extends View implements Runnable {

    private int delta;
    private boolean isGifStop = false;

    private Bitmap gifFrame;
    private GifOpenHelper mGifOpenHelper;

    private static int SPEED_NORMAL_VALUE = 4;

    public static int SPEED_SLOW = 2;
    public static int SPEED_NORMAL = 4;
    public static int SPEED_FAST = 8;

    public GifView(Context context) {
        this(context, null);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GifView);
        for (int i = 0; i < a.getIndexCount(); i++) {
            switch (a.getIndex(i)) {
                case R.styleable.GifView_src:
                    setGifSrc(a.getResourceId(R.styleable.GifView_src, 0));
                    break;
                case R.styleable.GifView_speed:
                    setGifSpeed(a.getInteger(R.styleable.GifView_speed, 1));
                    break;
                case R.styleable.GifView_gif_stop:
                    setGifStop(a.getBoolean(R.styleable.GifView_gif_stop, false));
                    break;
            }
        }
        a.recycle();
    }

    public void setGifStop(boolean isStop) {
        if (isStop) {
            isGifStop = true;
        } else {
            setGifStart();
        }
    }

    public void setGifStart() {
        isGifStop = false;

        Thread updateTimer = new Thread(this);
        updateTimer.start();
    }

    public void setGifSpeed(int del) {
        switch (Speed.mapIntToValue(del)) {
            case SLOW:
                this.delta = SPEED_NORMAL_VALUE / 2;
                break;
            case NORMAL:
                this.delta = SPEED_NORMAL_VALUE;
                break;
            case FAST:
                this.delta = SPEED_NORMAL_VALUE * 2;
                break;
        }
    }

    /**
     * set the gif speed,the method would disable xml "gif:speed" if you use.
     *
     * @param delta value: 4 is the normal speed,better set value range from 1 to 30.
     */
    public void setGifSpeedValue(int delta) {
        this.delta = delta;
    }

    public void setGifSrc(int rawId) {
        mGifOpenHelper = new GifOpenHelper();
        mGifOpenHelper.read(getResources().openRawResource(rawId));
        gifFrame = mGifOpenHelper.getFirstFrame();//get the first frame
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("dongdong", "widthMeasureSpec = " + widthMeasureSpec + "  heightMeasureSpec" + heightMeasureSpec);
        setMeasuredDimension(mGifOpenHelper.getWidth(), mGifOpenHelper.getHeight());
//        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(gifFrame, 0, 0, new Paint());
        gifFrame = mGifOpenHelper.nextBitmap();
    }

    @Override
    public void run() {
        while (!isGifStop) {
            try {
                this.postInvalidate();
                Log.d("dongdong", "nextdelay = " + mGifOpenHelper.nextDelay());
                Thread.sleep(mGifOpenHelper.nextDelay() * SPEED_NORMAL_VALUE / delta);
            } catch (Exception ex) {

            }
        }
    }

    public static enum Speed {

        SLOW(0x0),
        NORMAL(0x1),
        FAST(0x2);

        static Speed mapIntToValue(final int modeInt) {
            for (Speed value : Speed.values()) {
                if (modeInt == value.getIntValue()) {
                    return value;
                }
            }

            // If not, return default
            return getDefault();
        }

        static Speed getDefault() {
            return NORMAL;
        }

        private int mIntValue;

        // The modeInt values need to match those from attrs.xml
        Speed(int modeInt) {
            mIntValue = modeInt;
        }

        int getIntValue() {
            return mIntValue;
        }
    }
}
