package com.bullfrog.multinestedlayout.view;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingParent2;

import com.bullfrog.multinestedlayout.utils.ScreenUtilKt;

public class MultiNestedScrollView extends ScrollView implements NestedScrollingParent2, NestedScrollingChild2 {

    private int mTopHeight;
    private boolean mForceDisplayHeight = true;
    private int mPriority;

    private MultiNestedScrollingHelper mHelper = new MultiNestedScrollingHelper(this);

    public MultiNestedScrollView(Context context) {
        super(context);
        init();
    }

    public MultiNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Log.d("test", "cur view id is" + getTag());
        setNestedScrollingEnabled(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mForceDisplayHeight) {
            Activity activity = (Activity) getContext();
            // force height to be screen height, excluding status bar height and nav bar height
            int height = ScreenUtilKt.getDisplayHeight(activity);
            int width = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    // as parent
    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mHelper.onStopNestedScroll(target, type);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return mHelper.onStartNestedScroll(child, target, axes, type);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mHelper.onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        // parent 在 preScroll 和 child 自己 scroll 的消耗完后，调用该方法，如果仍有剩余，则 dyConsumed > 0

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        mHelper.onNestedPreScroll(target, dx, dy, consumed, type);
    }

    @Override
    public int getNestedScrollAxes() {
        return mHelper.getNestedScrollAxes();
    }

    // as child
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        mHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return mHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

        @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mTopHeight) {
            y = mTopHeight;
        }
        if (getScrollY() != y) {
            super.scrollTo(x, y);
        }
    }

    public int getTopHeight() {
        return mTopHeight;
    }

    public void setTopHeight(int mTopHeight) {
        this.mTopHeight = mTopHeight;
        mHelper.setTopHeight(mTopHeight);
    }

    public boolean isForceScreenHeight() {
        return mForceDisplayHeight;
    }

    public void setForceScreenHeight(boolean mForceScreenHeight) {
        this.mForceDisplayHeight = mForceScreenHeight;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int mPriority) {
        this.mPriority = mPriority;
    }

    public MultiNestedScrollingHelper getHelper() {
        return mHelper;
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return true;
    }
}
