package com.wdd.gif.movie;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.wdd.gif.R;

/**
 * Created by wangdd on 15-8-6.
 */
public class GifMovieView extends View {

    private Movie mMovie;
    private long mMovieStart;

    private int mMovieResId;
    private boolean mPaused;

    public GifMovieView(Context context) {
        this(context, null);
    }

    public GifMovieView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.GifMovieViewTheme_gifMovieViewStyle);
    }

    public GifMovieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setViewAttributes(context, attrs, defStyleAttr);
    }

    private void setViewAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        /**
         * Starting from HONEYCOMB(3.0) have to turn off HW acceleration to draw
         * Movie on Canvas.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GifMovieView, defStyleAttr, R.style.Widget_GifMovieView);
        mMovieResId = a.getResourceId(R.styleable.GifMovieView_gif, -1);
        mPaused = a.getBoolean(R.styleable.GifMovieView_paused, false);
        a.recycle();

        if (mMovieResId != -1) {
            mMovie = Movie.decodeStream(getResources().openRawResource(mMovieResId));
        }
    }

    private float mScale;
    private int mScaledMovieWidth;
    private int mScaledMovieHeight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMovie != null) {
            int mMovieWidth = mMovie.width();
            int mMovieHeight = mMovie.height();

            float scaleHor = 1f;
            int mModeWidth = MeasureSpec.getMode(widthMeasureSpec);
            if (mModeWidth != MeasureSpec.UNSPECIFIED) {
                int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
                if (mModeWidth > maxWidth) scaleHor = (float) mModeWidth / (float) maxWidth;
            }
            float scaleVer = 1f;
            int mModeHeight = MeasureSpec.getMode(heightMeasureSpec);
            if (mModeHeight != MeasureSpec.UNSPECIFIED) {
                int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (mModeHeight > maxHeight) scaleVer = (float) mModeWidth / (float) maxHeight;
            }

            mScale = 1f / Math.max(scaleHor, scaleVer);
            mScaledMovieWidth = (int) (mMovieWidth * mScale);
            mScaledMovieHeight = (int) (mMovieHeight * mScale);

            setMeasuredDimension(mScaledMovieWidth, mScaledMovieHeight);
        } else {
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    private float mLeft;
    private float mTop;
    private boolean mVisible = true;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mLeft = (getWidth() - mScaledMovieWidth) / 2f;
        mTop = (getHeight() - mScaledMovieHeight) / 2f;
        mVisible = getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie != null) {
            if (!mPaused) {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            } else {
                drawMovieFrame(canvas);
            }
        }
    }

    private void invalidateView() {
        if (mVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }

    private void drawMovieFrame(Canvas canvas) {
        mMovie.setTime(mCurrentAnimationTime);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScale, mScale);
        mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
        canvas.restore();
    }

    private static final int DEFAULT_MOVIE_DURATION = 1000;
    private int mCurrentAnimationTime = 0;

    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        int dur = mMovie.duration();
        if (dur == 0) {
            dur = DEFAULT_MOVIE_DURATION;
        }
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
        invalidateView();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    public void setMovieResource(int movieResId) {
        this.mMovieResId = movieResId;
        mMovie = Movie.decodeStream(getResources().openRawResource(mMovieResId));
        requestLayout();
    }

    public void setMovie(Movie movie) {
        this.mMovie = movie;
        requestLayout();
    }

    public Movie getMovie() {
        return mMovie;
    }

    public void setMovieTime(int time) {
        mCurrentAnimationTime = time;
        invalidate();
    }

    public void setPaused(boolean paused) {
        this.mPaused = paused;

        /**
         * Calculate new movie start time, so that it resumes from the same
         * frame.
         */
        if (!paused) {
            mMovieStart = android.os.SystemClock.uptimeMillis() - mCurrentAnimationTime;
        }

        invalidate();
    }

    public boolean isPaused() {
        return this.mPaused;
    }
}
