package com.example.floatwindowdemo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class DragFloatWrapper<T extends View> extends LinearLayout {

    private final float MIN_DISTANCE = dip2px(getContext(),3);
    private final float MOVEZONE_TOP = dip2px(getContext(),55);

    private static int screenWidthPixels;
    private static int screenHeightPixels;

    /**
     * 吸附动画
     */
    private ObjectAnimator adsorbAnim;

    private ObjectAnimator restoreYAnim;

    /**
     * 移动热区
     */
    private Rect moveZoneRect;

    private WindowTouchListener windowTouchListener;

    /**
     * 悬浮窗宽高
     */
    private int windowWidth, windowHeight;

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private T customView;

    /**
     * 控件拖动最终位置
     */
    private float lastX, lastY = 0;

    public DragFloatWrapper(Context context) {
        this(context, null);
    }

    public DragFloatWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DragFloatWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        adsorbAnim = ObjectAnimator.ofFloat(this, "windowX", 0, 1);
        adsorbAnim.setDuration(250);

        restoreYAnim = ObjectAnimator.ofFloat(this, "windowY", 0, 1);
        restoreYAnim.setDuration(250);

        if(moveZoneRect == null){
            moveZoneRect = new Rect();
            moveZoneRect.left = dip2px(context, 20);
            moveZoneRect.right = dip2px(context, 20);
            moveZoneRect.top = dip2px(context, 55);
            moveZoneRect.bottom = dip2px(context, 50);
        }

        windowManager = getWindowManager(context);
        windowParams = new WindowManager.LayoutParams();
    }

    /**
     * 自定义view
     *
     * @param customView
     */
    public void setCustomView(T customView) {
        this.customView = customView;
        removeAllViews();
        addView(customView);
        windowWidth = customView.getLayoutParams().width;
        windowHeight = customView.getLayoutParams().height;
    }

    public T getCustomView() {
        return customView;
    }

    /**
     * 间距 px
     *
     * @param moveZoneRect
     */
    public void setMoveZoneRect(Rect moveZoneRect) {
        this.moveZoneRect = moveZoneRect;
    }

    public Rect getMoveZoneRect() {
        return moveZoneRect;
    }

    /**
     * 更新位置
     *
     * @param x
     * @param y
     */
    public void setWindowPosition(float x, float y) {
        windowParams.x = (int) x;
        windowParams.y = (int) y;
        Log.d(getClass().getSimpleName(), "setWindowPosition****" + "X:" + x + "Y:" + y);
        try {
            windowManager.updateViewLayout(this, windowParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WindowManager.LayoutParams getWindowParams() {
        return windowParams;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowTouchListener(WindowTouchListener windowTouchListener) {
        this.windowTouchListener = windowTouchListener;
    }

    /**
     * 处理子view有点击事件造成的冲突
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float rowX = ev.getRawX();
        float rowY = ev.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = rowX;
                lastY = rowY;
                break;
            /**
             * 做滑动区间判断 否则很难点击
             */
            case MotionEvent.ACTION_MOVE:
                float dx = rowX - lastX;
                float dy = rowY - lastY;
                //视为点击 不拦截
                if(Math.abs(dx) < MIN_DISTANCE && Math.abs(dy) < MIN_DISTANCE){
                    break;
                }
                return true;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float rowX = ev.getRawX();
        float rowY = ev.getRawY();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                //计算移动了多少
                float dx = rowX - lastX;
                float dy = rowY - lastY;
                //设置当前位置
                setWindowPosition(fitXMoveZone(windowParams.x + dx), fitYMoveZone(windowParams.y + dy));
                //将当前位置设为最终的位置
                lastX = rowX;
                lastY = rowY;
                if (windowTouchListener != null) {
                    windowTouchListener.onMoveChanged(windowParams.x, windowParams.y);
                }
                //视为移动
                if(windowTouchListener != null){
                    if(Math.abs(dx) > MIN_DISTANCE || Math.abs(dy) > MIN_DISTANCE){
                        windowTouchListener.onActionMove();
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                if (windowTouchListener != null) {
                    windowTouchListener.onActionUp();
                }
                absorb(rowX);
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    private float fitXMoveZone(float x) {
        if (x > getScreenWidthPixels(getContext()) - moveZoneRect.right - this.windowWidth) {
            x = getScreenWidthPixels(getContext()) - moveZoneRect.right - this.windowWidth;
        } else if (x < moveZoneRect.left) {
            x = moveZoneRect.left;
        }
        return x;
    }

    private float fitYMoveZone(float y) {
        if (y > getScreenHeightPixels(getContext()) - getStatusBarHeight(getContext())) {
            y = getScreenHeightPixels(getContext()) - getStatusBarHeight(getContext());
        } else if (y < MOVEZONE_TOP) {
            y = MOVEZONE_TOP;
        }
        return y;
    }

    private void setWindowX(float windowX) {
        this.windowParams.x = (int) windowX;
        if (this.isShown()) {
            windowManager.updateViewLayout(this, windowParams);
        }
    }

    private void setWindowY(float windowY) {
        this.windowParams.y = (int) windowY;
        if (this.isShown()) {
            windowManager.updateViewLayout(this, windowParams);
        }
    }

    /**
     * 吸附动作
     *
     * @param rowX
     */
    private void absorb(float rowX) {
        //计算移动了多少
        float dx = rowX - lastX;
        float left = windowParams.x + dx;
        //判断当前控件距离哪边比较近
        if (left + (this.windowWidth / 2) > getScreenWidthPixels(getContext()) / 2) {
            //距离右边比较近
            left = (getScreenWidthPixels(getContext()) - (windowWidth) - moveZoneRect.right);
        } else {
            //距离左边比较近
            left = moveZoneRect.left;
        }
        //设置当前位置
        if (adsorbAnim != null) {
            adsorbAnim.setFloatValues(windowParams.x + dx, left);
            adsorbAnim.start();
        }
        //将当前位置设为最终的位置
        lastX = rowX;
    }

    public void resoreY(int initY) {
        if (restoreYAnim != null) {
            restoreYAnim.setFloatValues(windowParams.y, initY);
            restoreYAnim.start();
        }
    }

    public interface WindowTouchListener {
        /**
         * 移动监听
         *
         * @param x
         * @param y
         */
        void onMoveChanged(float x, float y);

        /**
         * 松手回调
         */
        void onActionUp();

        /**
         * 移动回调
         */
        void onActionMove();
    }


    public static int dip2px(Context context, float dipValue) {
        if (context == null) {
            return (int) dipValue;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static WindowManager getWindowManager(Context context) {
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * @param context
     * @return
     */
    public static int getScreenWidthPixels(Context context) {

        if (context == null) {
            return 0;
        }

        if (screenWidthPixels > 0) {
            return screenWidthPixels;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        screenWidthPixels = dm.widthPixels;
        return screenWidthPixels;
    }

    /**
     * @param context
     * @return
     */
    public static int getScreenHeightPixels(Context context) {
        if (context == null) {
            return 0;
        }

        if (screenHeightPixels > 0) {
            return screenHeightPixels;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        screenHeightPixels = dm.heightPixels;
        return screenHeightPixels;
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

}
