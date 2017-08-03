package com.blingbling.tipprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by BlingBling on 2017/8/3.
 */

public class TipProgressBar extends View {

    private static final int TIP_PADDING_HORIZONTAL = 4;
    private static final int TIP_PADDING_VERTICAL = 2;
    private static final int TIP_RADIUS = 3;
    private static final int TIP_TEXT_SIZE = 12;

    private static final int PROGRESS_HEIGHT = 4;
    private static final int COLOR_TIP = 0XFF0000FF;
    private static final int COLOR_TIP_TEXT = 0XFFFFFFFF;
    private static final int COLOR_PROGRESS_BACKGROUND = 0XFF999999;
    private static final int COLOR_PROGRESS = 0XFF0000FF;

    private static final String TIP_TEXT_FORMAT = "%d%%";

    private static final int DEFAULT_MAX_PROGRESS = 100;
    private static final int DEFAULT_PROGRESS = 0;

    //提示框
    private int mTipPaddingHorizontal;
    private int mTipPaddingVertical;
    private int mTipRadius;
    private float mTipWidth;
    private float mTipHeight;
    private float mTriangleHeight;
    private RectF mTipRectangle = new RectF();
    private Path mTipTriangle = new Path();
    //进度条高度
    private int mProgressHeight;
    //颜色
    private int mTipColor;
    private int mTipTextColor;
    private int mProgressBackgroundColor;
    private int mProgressColor;

    private int mTipTextSize;

    private Paint mProgressPaint;
    private Paint mTipPaint;
    private Paint mTextPaint;

    private int mMaxProgress;
    private int mCurrentProgress;

    private String mTipTextFormat;

    public TipProgressBar(Context context) {
        this(context, null);
    }

    public TipProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TipProgressBar);
        mTipPaddingHorizontal = a.getDimensionPixelOffset(R.styleable.TipProgressBar_tpbTipPaddingHorizontal, dp2px(TIP_PADDING_HORIZONTAL));
        mTipPaddingVertical = a.getDimensionPixelOffset(R.styleable.TipProgressBar_tpbTipPaddingVertical, dp2px(TIP_PADDING_VERTICAL));
        mTipRadius = a.getDimensionPixelOffset(R.styleable.TipProgressBar_tpbTipRadius, dp2px(TIP_RADIUS));
        mTipTextSize = a.getDimensionPixelOffset(R.styleable.TipProgressBar_tpbTipTextSize, dp2px(TIP_TEXT_SIZE));
        mProgressHeight = a.getDimensionPixelOffset(R.styleable.TipProgressBar_tpbProgressHeight, dp2px(PROGRESS_HEIGHT));

        mTipColor = a.getColor(R.styleable.TipProgressBar_tpbTipColor, COLOR_TIP);
        mTipTextColor = a.getColor(R.styleable.TipProgressBar_tpbTipTextColor, COLOR_TIP_TEXT);
        mProgressBackgroundColor = a.getColor(R.styleable.TipProgressBar_tpbProgressBackgroundColor, COLOR_PROGRESS_BACKGROUND);
        mProgressColor = a.getColor(R.styleable.TipProgressBar_tpbProgressColor, COLOR_PROGRESS);

        mTipTextFormat = a.getString(R.styleable.TipProgressBar_tpbTipTextFormat);
        if (TextUtils.isEmpty(mTipTextFormat)) {
            mTipTextFormat = TIP_TEXT_FORMAT;
        }
        mMaxProgress = a.getInt(R.styleable.TipProgressBar_tpbMaxProgress, DEFAULT_MAX_PROGRESS);
        mCurrentProgress = a.getInt(R.styleable.TipProgressBar_tpbProgress, DEFAULT_PROGRESS);
        setMaxProgress(mMaxProgress);
        setProgress(mCurrentProgress);
        a.recycle();
        initPaint();
        computeTipSize();
    }

    private void initPaint() {
        mTipPaint = getPaint(mProgressHeight, mTipColor);
        mProgressPaint = getPaint(mProgressHeight, mProgressColor);
        initTextPaint();
    }

    /**
     * 获取画笔
     *
     * @param strokeWidth
     * @param color
     * @return
     */
    private Paint getPaint(int strokeWidth, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    /**
     * 文本画笔
     */
    private void initTextPaint() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTipTextSize);
        mTextPaint.setColor(mTipTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    /**
     * 测量控件宽度
     *
     * @param widthMeasureSpec
     * @return
     */
    private int measureWidth(int widthMeasureSpec) {
        return MeasureSpec.getSize(widthMeasureSpec);
    }

    /**
     * 测量空间高度
     *
     * @param heightMeasureSpec
     * @return
     */
    private int measureHeight(int heightMeasureSpec) {
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int measureHeight = size;
        int minHeight = (int) (getPaddingTop() + getPaddingBottom() + mTipHeight + mProgressHeight);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                measureHeight = minHeight;
                break;
            case MeasureSpec.EXACTLY:
                measureHeight = Math.max(minHeight, size);
                break;
        }
        return measureHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight() - mProgressHeight;
        float progressPosition = getPaddingLeft() + 1.0f * mCurrentProgress / mMaxProgress * width + mProgressHeight / 2;
        drawTip(canvas, progressPosition);
        drawText(canvas);
        drawProgress(canvas, progressPosition);
    }

    /**
     * 绘制提示框
     *
     * @param canvas
     */
    private void drawTip(Canvas canvas, float progressPosition) {
//        float maxMoveWidth = getWidth() - getPaddingLeft() - getPaddingRight() - mTipWidth;
//        float x = getPaddingLeft() + 1.0f * mCurrentProgress / mMaxProgress * maxMoveWidth;
//        float x = progressPosition;
        float y = getPaddingTop();
        //如果在两边就固定不移动
        progressPosition -= mTipWidth / 2;
        if (progressPosition < getPaddingLeft()) {
            progressPosition = getPaddingLeft();
        } else if (progressPosition + mTipWidth > getWidth() - getPaddingRight()) {
            progressPosition = getWidth() - getPaddingRight() - mTipWidth;
        }
        //画矩形
        mTipRectangle.set(progressPosition,
                y,
                progressPosition + mTipWidth,
                y + mTipHeight - mTriangleHeight);
        canvas.drawRoundRect(mTipRectangle, mTipRadius, mTipRadius, mTipPaint);
        //画三角形
        mTipTriangle.reset();
        mTipTriangle.moveTo(mTipRectangle.centerX() - mTriangleHeight, mTipRectangle.bottom);
        mTipTriangle.lineTo(mTipRectangle.centerX() + mTriangleHeight, mTipRectangle.bottom);
        mTipTriangle.lineTo(mTipRectangle.centerX(), mTipRectangle.bottom + mTriangleHeight);
        mTipTriangle.close();
        canvas.drawPath(mTipTriangle, mTipPaint);
    }

    /**
     * 绘制提示文本
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
//        文本居中算法
//        mTipRectangle.centerY() - (FontMetrics.bottom - FontMetrics.top) / 2 - FontMetrics.top
//        优化后即：
//        (mTipRectangle.bottom + mTipRectangle.top - fontMetrics.bottom - fontMetrics.top) / 2

        String text = getProgressTipText(mCurrentProgress, mMaxProgress);

        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        float baseline = (mTipRectangle.top + mTipRectangle.bottom - metrics.bottom - metrics.top) / 2;
        canvas.drawText(text, mTipRectangle.centerX(), baseline, mTextPaint);
    }

    /**
     * 绘制进度条
     *
     * @param canvas
     */
    private void drawProgress(Canvas canvas, float progressPosition) {
        //进度条的高度一半，两边要处理下才能显示圆角
        int wh = mProgressHeight / 2;
        float y = getPaddingTop() + mTipHeight + wh;
        mProgressPaint.setColor(mProgressBackgroundColor);
        canvas.drawLine(progressPosition, y, getWidth() - getPaddingRight() - wh, y, mProgressPaint);
        mProgressPaint.setColor(mProgressColor);
        canvas.drawLine(getPaddingLeft() + wh, y, progressPosition, y, mProgressPaint);
    }

    /**
     * 获取进度条提示文本
     *
     * @param progress
     * @param maxProgress
     * @return
     */
    private String getProgressTipText(int progress, int maxProgress) {
        return String.format(mTipTextFormat, (int) 100.0 * progress / maxProgress);
    }

    /**
     * dp转px
     *
     * @param value
     */
    public int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, getResources().getDisplayMetrics());
    }

    /**
     * 设置提示框padding
     *
     * @param horizontal
     * @param vertical
     * @return
     */
    public TipProgressBar setTipPadding(int horizontal, int vertical) {
        mTipPaddingHorizontal = dp2px(horizontal);
        mTipPaddingVertical = dp2px(vertical);
        computeTipSize();
        requestLayout();
        return this;
    }

    /**
     * 设置提示框圆角弧度
     *
     * @param tipRadius
     * @return
     */
    public TipProgressBar setTipRadius(int tipRadius) {
        mTipRadius = dp2px(tipRadius);
        computeTipSize();
        postInvalidate();
        return this;
    }

    /**
     * 设置提示文本大小
     *
     * @param textSize
     */
    public TipProgressBar setTipTextSize(int textSize) {
        mTipTextSize = dp2px(textSize);
        mTextPaint.setTextSize(mTipTextSize);
        computeTipSize();
        requestLayout();
        return this;
    }

    /**
     * 设置提示文本样式,默认是 %d%%
     *
     * @param format
     */
    public TipProgressBar setTipTextFormat(String format) {
        mTipTextFormat = format;
        computeTipSize();
        requestLayout();
        return this;
    }

    /**
     * 设置提示框颜色
     *
     * @param color
     * @return
     */
    public TipProgressBar setTipColor(@ColorInt int color) {
        mTipColor = color;
        postInvalidate();
        return this;
    }

    /**
     * 设置提示框文本颜色
     *
     * @param color
     * @return
     */
    public TipProgressBar setTipTextColor(@ColorInt int color) {
        mTipTextColor = color;
        postInvalidate();
        return this;
    }

    /**
     * 设置进度条背景颜色
     *
     * @param color
     * @return
     */
    public TipProgressBar setProgressBackgroundColor(@ColorInt int color) {
        mProgressBackgroundColor = color;
        postInvalidate();
        return this;
    }

    /**
     * 设置进度条颜色
     *
     * @param color
     * @return
     */
    public TipProgressBar setProgressColor(@ColorInt int color) {
        mProgressColor = color;
        postInvalidate();
        return this;
    }

    /**
     * 设置最大进度
     *
     * @param maxProgress
     * @return
     */
    public TipProgressBar setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
        if (mMaxProgress <= 0) {
            mMaxProgress = DEFAULT_MAX_PROGRESS;
        }
        if (mCurrentProgress > mMaxProgress) {
            mCurrentProgress = mMaxProgress;
        }
        postInvalidate();
        return this;
    }

    /**
     * 设置当前进度
     *
     * @param progress
     * @return
     */
    public TipProgressBar setProgress(int progress) {
        this.mCurrentProgress = progress;
        if (mCurrentProgress < 0) {
            mCurrentProgress = 0;
        }
        if (mCurrentProgress > mMaxProgress) {
            mCurrentProgress = mMaxProgress;
        }
        postInvalidate();
        return this;
    }

    /**
     * 计算提示框大小
     */
    private void computeTipSize() {
        String text = getProgressTipText(100, 100);
        float textWidth = mTextPaint.measureText(text);
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        float textHeight = metrics.bottom - metrics.top;

        mTipWidth = mTipPaddingHorizontal * 2 + textWidth;
        mTipHeight = mTipPaddingVertical * 2 + textHeight;
        //三角形的高度
        mTriangleHeight = mTipHeight / 4;
        mTipHeight = mTipHeight + mTriangleHeight;
    }
}
