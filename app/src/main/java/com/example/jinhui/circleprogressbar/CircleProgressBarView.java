package com.example.jinhui.circleprogressbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.jinhui.circleprogressbar.utils.ScreenUtils;

/**
 * 一个带有数字的圆形loading
 */
public class CircleProgressBarView extends View {

    private static final String TAG = CircleProgressBarView.class.getSimpleName();
    //外圈的宽度
    private float borderWidth;
    //未达到的进度的颜色
    private int unReachedColor;
    //到达的进度颜色
    private int reachedColor;
    //中间数字的颜色
    private int numberColor;
    //中间数字的大小
    private float numberSize;
    //是否带有色彩的进度样式
    private boolean isColorFul;

    private Paint unReachedPaint;
    private Paint reachedPaint;
    private Paint numPaint;
    private String maxPercent = "100%";
    private int width, height;

    private int progress;

    private int startNumColor;
    private int endNumColor;
    //用于圆弧相切的矩形
    private RectF arcRect;
    private float radius;

    private float centerX;
    private float centerY;

    //起始数字的大小
    private float startNumScale;
    //放大后数字的大小
    private float endNumScale;

    private int startColorFulColor;
    private int endColorFulColor;

    private ObjectAnimator colorFulAnimator;
    private ObjectAnimator numScaleAnimator;
    private AnimatorSet animationSet;

    private boolean hasDraw;

    public CircleProgressBarView(Context context) {
        super(context);
    }

    public CircleProgressBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public CircleProgressBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    private void init(AttributeSet attrs, Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBarView);
        borderWidth = typedArray.getDimension(R.styleable.CircleProgressBarView_border_width, ScreenUtils.dp2px(context, 3));
        unReachedColor = typedArray.getColor(R.styleable.CircleProgressBarView_unreached_color, Color.parseColor("#87CEFA"));
        reachedColor = typedArray.getColor(R.styleable.CircleProgressBarView_reached_color, Color.parseColor("#FF0000"));
        numberColor = typedArray.getColor(R.styleable.CircleProgressBarView_number_color, Color.parseColor("#DC143C"));
        numberSize = typedArray.getDimension(R.styleable.CircleProgressBarView_number_size, ScreenUtils.sp2px(context, 15));
        startNumScale = numberSize;
        endNumScale = ScreenUtils.sp2px(context, ScreenUtils.px2sp(context, startNumScale) + 2);
        isColorFul = typedArray.getBoolean(R.styleable.CircleProgressBarView_isColorful, false);
        progress = typedArray.getInt(R.styleable.CircleProgressBarView_number_progress, 0);
        if (unReachedColor == reachedColor) {
            throw new IllegalArgumentException("the unreadchColor is not different from reachedColor");
        }

        // 初始化画笔
        unReachedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unReachedPaint.setColor(unReachedColor);
        unReachedPaint.setStrokeWidth(borderWidth);
        unReachedPaint.setStyle(Paint.Style.STROKE);

        reachedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        reachedPaint.setColor(reachedColor);
        reachedPaint.setStrokeWidth(borderWidth);
        reachedPaint.setStyle(Paint.Style.STROKE);

        numPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numPaint.setColor(numberColor);
        numPaint.setTextSize(numberSize);
        numPaint.setStrokeWidth(borderWidth);

        startNumColor = numberColor;
        endNumColor = Color.parseColor("#FFA500");
        arcRect = new RectF();

        startColorFulColor = reachedColor;
        endColorFulColor = Color.parseColor("#0000FF");

        typedArray.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!hasDraw) {
            width = getWidth();
            height = getHeight();
            radius = (float) (Math.min(width, height) * 1.0 / 2) - borderWidth;
            centerX = (float) (width * 1.0 / 2);
            centerY = (float) (height * 1.0 / 2);
            arcRect.left = centerX - radius;
            arcRect.top = centerY - radius;
            arcRect.right = centerX + radius;
            arcRect.bottom = centerY + radius;
            hasDraw = true;
            Log.d(TAG, "width:" + width);
            Log.d(TAG, "height:" + height);
        }
        numPaint.setTextSize(numberSize);
        numPaint.setColor(numberColor);

        canvas.drawCircle(centerX, centerY, radius, unReachedPaint);
        String str = String.valueOf(progress) + "%";
        float numWidth = numPaint.measureText(str);

        canvas.drawText(str, (float) (width * 1.0 / 2 - numWidth * 1.0 / 2), (float) (height * 1.0 / 2 + getTextHeight(str) * 1.0 / 2), numPaint);
        if (!isColorFul) {
            canvas.drawArc(arcRect, -90, (float) (progress * 360 / 100), false, reachedPaint);
        } else {
            reachedPaint.setColor(reachedColor);
            canvas.drawArc(arcRect, -90 + (float) (progress * 360 / 100), 90, false, reachedPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasure(widthMeasureSpec, true), getMeasure(heightMeasureSpec, false));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private int getMeasure(int measureSpec, boolean isWidth) {
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        Log.d(TAG, "size" + size);
        Log.d(TAG, "measure mode:" + mode);
        if (mode == MeasureSpec.EXACTLY) {
            int temp = isWidth ? padding + getMinWidth() * 3 : padding + getMinHeight() * 3;
            size = Math.max(temp, size);
            return size;
        } else {
            size = isWidth ? padding + getMinWidth() * 3 : padding + getMinHeight() * 3;
            return size;
        }
    }

    private int getMinHeight() {
        return getTextHeight(maxPercent);
    }

    /**
     * 获取最小的宽度，以中间的数字为标准
     *
     * @return
     */
    private int getMinWidth() {
        return (int) (numPaint.measureText(maxPercent) + 0.5);
    }

    /**
     * 获取字的最小高度
     *
     * @return
     */
    private int getTextHeight(String str) {
        Rect rect = new Rect();
        numPaint.getTextBounds(str, 0, str.length(), rect);
        return rect.height();
    }

    public void setProgress(int progress) {
        if (this.progress != progress) {
            this.progress = progress;
            //这里每次进来的时候让那个中间数字进行放大一下
            startNumAnimation();
            if (listener != null) {
                listener.onChange(this.progress);
            }
        }
    }


    public void setIsColorFul(boolean isColorFul) {
        this.isColorFul = isColorFul;
        invalidate();
    }


    public float getNumberSize() {
        return numberSize;
    }

//    public void setNumberSize(float numberSize) {
//        this.numberSize = numberSize;
//        this.numberSize = numberSize;
//        numPaint.setTextSize(this.numberSize);
//        invalidate();
//    }

    public void setNumberSize(float numberSize) {
        if (numberSize > 2 * this.numberSize) {
            throw new RuntimeException("you do not set the number textSize twice as big as your before your setting");
        }
        this.numberSize = numberSize;
        numPaint.setTextSize(this.numberSize);
        invalidate();
    }

    public int getNumberColor() {
        return numberColor;
    }

    /**
     * @param numberColor
     */
    public void setNumberColor(int numberColor) {
        this.numberColor = numberColor;
        numPaint.setColor(this.numberColor);
        invalidate();
    }

    public int getReachedColor() {
        return reachedColor;
    }

    public void setReachedColor(int reachedColor) {
        this.reachedColor = reachedColor;
        reachedPaint.setColor(this.reachedColor);
        invalidate();
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        unReachedPaint.setStrokeWidth(this.borderWidth);
        reachedPaint.setStrokeWidth(this.borderWidth);
        invalidate();
    }

    public void setUnReachedColor(int unReachedColor) {
        this.unReachedColor = unReachedColor;
        unReachedPaint.setColor(this.unReachedColor);
        invalidate();
    }


    Property<CircleProgressBarView, Float> numScaleProperty = new Property<CircleProgressBarView, Float>(Float.class, "numberSize") {
        @Override
        public Float get(CircleProgressBarView object) {
            return object.getNumberSize();
        }

        @Override
        public void set(CircleProgressBarView object, Float value) {
            object.setNumberSize(value);
        }
    };

    Property<CircleProgressBarView, Integer> numColorProperty = new Property<CircleProgressBarView, Integer>(Integer.class, "numberColor") {
        @Override
        public Integer get(CircleProgressBarView object) {
            return object.getNumberColor();
        }

        @Override
        public void set(CircleProgressBarView object, Integer value) {
            object.setNumberColor(value);
        }
    };

    Property<CircleProgressBarView, Integer> colorFulColorProperty = new Property<CircleProgressBarView, Integer>(Integer.class, "reachedColor") {
        @Override
        public Integer get(CircleProgressBarView object) {
            return object.getReachedColor();
        }

        @Override
        public void set(CircleProgressBarView object, Integer value) {
            object.setReachedColor(value);
        }
    };

    private void startNumAnimation() {
        numScaleAnimator = ObjectAnimator.ofFloat(this, numScaleProperty, startNumScale, endNumScale);
        numScaleAnimator.setDuration(200);
        numScaleAnimator.setRepeatCount(1);
        numScaleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        numScaleAnimator.setInterpolator(new OvershootInterpolator(2));

        if (Build.VERSION.SDK_INT >= 21) {
            final ObjectAnimator numColorAnimator = ObjectAnimator.ofArgb(this, numColorProperty,
                    startNumColor,
                    Color.parseColor("#0000CD"),
                    Color.parseColor("#191970"),
                    Color.parseColor("#00008B"),
                    Color.parseColor("#000080"),
                    Color.parseColor("#4169E1"),
                    Color.parseColor("#6495ED"),
                    endNumColor);
            numScaleAnimator.setDuration(200);
            numScaleAnimator.setRepeatCount(1);
            numScaleAnimator.setRepeatMode(ValueAnimator.REVERSE);
            numColorAnimator.setInterpolator(new LinearInterpolator());
            if (isColorFul) {
                colorFulAnimator = ObjectAnimator.ofArgb(this, colorFulColorProperty,
                        startColorFulColor,
                        Color.parseColor("#0000CD"),
                        Color.parseColor("#191970"),
                        Color.parseColor("#00008B"),
                        Color.parseColor("#000080"),
                        Color.parseColor("#4169E1"),
                        Color.parseColor("#6495ED"),
                        endColorFulColor);
                colorFulAnimator.setDuration(500);
                colorFulAnimator.setRepeatCount(1);
                colorFulAnimator.setRepeatMode(ValueAnimator.REVERSE);
                colorFulAnimator.setInterpolator(new LinearInterpolator());
                colorFulAnimator.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        colorFulAnimator.cancel();
                    }
                });
            }
            animationSet = new AnimatorSet();
            if (isColorFul) {
                animationSet.playTogether(numScaleAnimator, numColorAnimator, colorFulAnimator);
            } else {
                animationSet.playTogether(numScaleAnimator, numColorAnimator);
            }
            numColorAnimator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    numColorAnimator.cancel();
                }
            });
            animationSet.start();
        } else {
            numScaleAnimator.start();
        }
        numScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        numScaleAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                numScaleAnimator.cancel();
            }
        });
    }


    public void cancel() {
        if (Build.VERSION.SDK_INT >= 21) {
            animationSet.cancel();
        } else {
            numScaleAnimator.cancel();
        }
    }

    private OnProgressChangeListener listener;

    public interface OnProgressChangeListener {
        void onChange(int progress);
    }

    public void setListener(OnProgressChangeListener listener) {
        this.listener = listener;
    }


    private class SimpleAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }


}
