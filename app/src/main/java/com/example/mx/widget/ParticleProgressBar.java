package com.example.mx.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.example.mx.particleprogressbar.R;
import com.example.mx.utils.Log;
import com.plattysoft.leonids.ParticleSystem;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by admin on 2017/11/4.
 */

public class ParticleProgressBar extends RelativeLayout {
    private static final String TAG = ParticleProgressBar.class.getSimpleName();
    private boolean isRunning = false;
    private int MAX = 500;
    private int STEP1 = 150;
    private int STEP1_MILLISECONDS = 15 * 1000;
    private long DURATION = 50000;
    private RelativeLayout pbBack;
    private View pbProgress;
    private TextView pbText;
    private ValueAnimator mValueAnimator;
    private int measuredWidth;
    private int measuredHeight;
    private int screenWidth;
    private int screenHeight;
    private int statusbarHeight;
    private LayoutParams lpProgress;
    private Animator.AnimatorListener mListener;
    private ParticleSystem mParticleSystem;
    private boolean mFirstChangedLayout = true;
    private int mParticleX = 0;
    private int mParticleY = 0;
    boolean flagStep1 = false;

    public ParticleProgressBar(Context context) {
        this(context, null);
        //Log.d(TAG, "ParticleProgressBar(Context context)");
    }

    public ParticleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        //Log.d(TAG, "ParticleProgressBar(Context context, AttributeSet attrs)");
    }

    public ParticleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        //Log.d(TAG, "ParticleProgressBar. " + screenWidth + ", " + screenHeight);
        //Log.d(TAG, "ParticleProgressBar(Context context, AttributeSet attrs, int defStyleAttr)");
        lpProgress = new LayoutParams(dp2px(1), LayoutParams.MATCH_PARENT);
        lpProgress.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        View view = LayoutInflater.from(context).inflate(R.layout.view_particle_progressbar, this, true);
        pbBack = (RelativeLayout) view.findViewById(R.id.pbBack);
        pbProgress = (View) view.findViewById(R.id.pbProgress);
        pbText = (TextView) view.findViewById(R.id.pbText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFirstChangedLayout) {//避免每次onMeasure都赋值影响性能
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
            Log.d(TAG, "onMeasure. " + measuredWidth + ", " + measuredHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //Log.d(TAG, "onLayout. changed=" + changed + ", " + l + ", " + t + ", " + r + ", " + b);
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            //每次移动内部控件时会重新设置其layoutparams，这样就防止父布局重绘时移动的内部控件又回到原点的问题。所以此view里只会执行一次changed==true。
            if (mFirstChangedLayout) {//避免每次onMeasure都赋值影响性能。记录当前view的根视图位置(屏幕坐标)
                mParticleX = getLeft();
                mParticleY = getTop() + measuredHeight / 2;
                Log.d(TAG, "onLayout. mFirstChangedLayout. " + getLeft() + ", " + getTop() + ", " + measuredHeight);
                Log.d(TAG, "onLayout. mFirstChangedLayout. " + mParticleX + ", " + mParticleY);
            }
            mFirstChangedLayout = false;
        }
    }

    /**
     * 进度从0开始
     *
     * @param viewGroup 整屏的view，用于放置Particle和计算屏幕内容区域高度。
     */
    public void start(final ViewGroup viewGroup, final OnProgressListener onProgressListener) {
        if (statusbarHeight == 0) {
            statusbarHeight = screenHeight - viewGroup.getHeight();//为了以后的兼容性，这里没有用反射等计算状态栏高度。
            mParticleY += statusbarHeight;
        }
        Log.d(TAG, "start. " + screenHeight + ", " + viewGroup.getHeight() + ", " + statusbarHeight + ", " + mParticleY);
        //Log.d(TAG, "start. " + isRunning + ", " + pbProgress.getMeasuredWidth() + ", " + pbProgress.getLeft() + ", " + pbProgress.getX());
        pbText.setText(new StringBuilder().append("0%").toString());
        showParticle(viewGroup);
        if (!isRunning) {
            lpProgress.leftMargin = 0;
            lpProgress.setMargins(0, 0, 0, 0);
            pbProgress.setLayoutParams(lpProgress);
            lpProgress.height = dp2px(22);
            mValueAnimator = ValueAnimator.ofInt(0, MAX);
            mValueAnimator.setDuration(DURATION);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    int currentValue = (Integer) animator.getAnimatedValue();
                    if (currentValue != 0) {
                        int currentOffset = measuredWidth * currentValue / MAX;
                        lpProgress.leftMargin = currentOffset;
                        lpProgress.setMargins(currentOffset, 0, 0, 0);
                        lpProgress.height = dp2px(22);
                        pbProgress.setLayoutParams(lpProgress);
                        pbText.setText(new StringBuilder().append(currentValue * 100 / MAX).append("%").toString());
                        mParticleSystem.updateEmitPoint(mParticleX + currentOffset, mParticleY);
                        //Log.d(TAG, "start. onAnimationUpdate. " + currentValue);
                        if (!flagStep1 && animator.getCurrentPlayTime() > STEP1_MILLISECONDS) {
                            Log.d(TAG, "start. onAnimationUpdate. STEP1_MILLISECONDS");
                            onProgressListener.onStep1();
                            flagStep1 = true;
                        }
                    }
                }
            });
            mListener = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isRunning = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d(TAG, "start.onAnimationEnd. ");
                    isRunning = false;
                    cancelParticle();
                    onProgressListener.onFinish();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    Log.d(TAG, "start.onAnimationCancel. ");
                    isRunning = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            };
            mValueAnimator.addListener(mListener);
            mValueAnimator.start();
        }
    }

    /**
     * 进度迅速平滑的走到结束。
     *
     * @param duration 走完剩余进度所用的毫秒数
     */
    public void finish(long duration, final OnProgressListener onProgressListener) {
        Log.d(TAG, "finish. " + isRunning);
        if (!isRunning) {
            return;
        }
        if (mValueAnimator != null) {
            mValueAnimator.removeListener(mListener);
            mValueAnimator.cancel();
        }
        final int completedWidth = lpProgress.leftMargin;// 已完成的进度
        final int remainedWidth = measuredWidth - lpProgress.leftMargin;// 还剩余的进度
        Log.d(TAG, "finish. " + completedWidth + ", " + remainedWidth);
        mValueAnimator = ValueAnimator.ofInt(0, MAX);
        mValueAnimator.setDuration(duration);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentValue = (Integer) animator.getAnimatedValue();
                if (currentValue != 0) {
                    int currentOffset = completedWidth + remainedWidth * currentValue / MAX;
                    lpProgress.leftMargin = currentOffset;
                    lpProgress.setMargins(currentOffset, 0, 0, 0);
                    lpProgress.height = dp2px(22);
                    pbProgress.setLayoutParams(lpProgress);
                    pbText.setText(new StringBuilder().append(currentValue * 100 / MAX).append("%").toString());
                    mParticleSystem.updateEmitPoint(mParticleX + currentOffset, mParticleY);
                    //Log.d(TAG, "finish. onAnimationUpdate. " + currentValue);
                }
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "finish.onAnimationEnd. ");
                isRunning = false;
                cancelParticle();
                onProgressListener.onFinish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "finish.onAnimationCancel. ");
                isRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mValueAnimator.start();
    }

    /**
     * 取消进度，cancel动画。
     */
    public void cancel() {
        Log.d(TAG, "cancel. " + isRunning);
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            cancelParticle();
        }
    }

    /**
     * 销毁时调用，cancel动画。
     */
    public void clearOnDestroy() {
        Log.d(TAG, "clear. " + isRunning);
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            cancelParticle();
        }
    }

    private void showParticle(ViewGroup viewGroup) {
        if (mParticleSystem != null) {
            mParticleSystem.cancel();
        }
        //第一个参数是activity，第二个是最多的粒子数，第三个是粒子的图片资源，第四个是持续时间，毫秒制，默认持续时间结束后会从开始重复。
        mParticleSystem = new ParticleSystem(viewGroup, 120, getResources().getDrawable(R.drawable.icon_guang), 1600);
        mParticleSystem.setScaleRange(0.1f, 1.0f);//设置粒子初始化时的比例大小区间
        mParticleSystem.setSpeedModuleAndAngleRange(0.006f, 0.09f, 110, 250);//置速度和发射角度
        //ps.setRotationSpeedRange(90, 180);
        mParticleSystem.setAcceleration(0.00000008f, 180);//速度(数越小越慢),方向(向左)
        //ps.setFadeOut(500, new AccelerateInterpolator());
        //mParticleSystem.emit(mParticleX, mParticleY, 80);
//        mParticleSystem.emit(mParticleX, mParticleY, 75);
        pbProgress.setMinimumHeight(dp2px(22));
        mParticleSystem.emitWithGravity(pbProgress,Gravity.LEFT,75);
        android.util.Log.e(TAG, "showParticle: "+pbProgress.getHeight()+"===="+pbProgress.getMeasuredHeight() );
    }

    private void cancelParticle() {
        if (mParticleSystem != null) {
            try {
                mParticleSystem.stopEmitting();
                mParticleSystem.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * dp 2 px
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getResources().getDisplayMetrics());
    }

    public interface OnProgressListener {
        public void onStep1();

        public void onFinish();
    }
}