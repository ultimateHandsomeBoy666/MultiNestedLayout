package com.bullfrog.multinestedlayout.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.bullfrog.multinestedlayout.R;
import com.bullfrog.multinestedlayout.utils.ScreenUtilKt;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class MultiNestedScrollView extends LinearLayout implements NestedScrollingParent2, NestedScrollingChild2 {

    private int mTopHeight;
    private boolean mForceDisplayHeight;
    private int mScrollUpPriority;
    private int mScrollDownPriority;

    private MultiNestedScrollingHelper mHelper = new MultiNestedScrollingHelper(this);

    public MultiNestedScrollView(Context context) {
        this(context, null);
    }

    public MultiNestedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiNestedScrollView);
        mTopHeight = a.getDimensionPixelSize(R.styleable.MultiNestedScrollView_topHeight, 0);
        mScrollUpPriority = a.getInteger(R.styleable.MultiNestedScrollView_scrollUpPriority, 0);
        mScrollDownPriority = a.getInteger(R.styleable.MultiNestedScrollView_scrollDownPriority, 0);
        mForceDisplayHeight = a.getBoolean(R.styleable.MultiNestedScrollView_forceDisplayHeight, true);
        a.recycle();
    }

    private void init() {
        setNestedScrollingEnabled(true);
        setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("onMeasure", "mode = " + Integer.toBinaryString(MeasureSpec.getMode(widthMeasureSpec)));
        if (mForceDisplayHeight) {
            Activity activity = (Activity) getContext();
            // force height to be screen height, excluding status bar height and nav bar height
            int height = ScreenUtilKt.getDisplayHeight(activity);
            int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        if (child instanceof NestedScrollingChild2) {
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            Activity activity = (Activity) getContext();

            final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft()
                    + getPaddingRight(), lp.width);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    ScreenUtilKt.getDisplayHeight(activity), MeasureSpec.UNSPECIFIED);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
        }
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        if (child instanceof  NestedScrollingChild2) {
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            Activity activity = (Activity) getContext();

            final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                            + widthUsed, lp.width);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    ScreenUtilKt.getDisplayHeight(activity), MeasureSpec.UNSPECIFIED);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
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

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mHelper.onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        // parent 在 preScroll 和 child 自己 scroll 的消耗完后，调用该方法，如果仍有剩余，则 dyConsumed > 0

    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        mHelper.onNestedPreScroll(target, dx, dy, consumed, type);
    }

    public void onNestedPreScrollActual(int dx, int dy, @NonNull int[] consumed) {
        mHelper.onNestedPreScrollActual(dx, dy, consumed);
    }

    @Override
    public int getNestedScrollAxes() {
        return mHelper.getNestedScrollAxes();
    }

    // as child
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

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

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

    public int getScrollUpPriority() {
        return mScrollUpPriority;
    }

    public void setScrollUpPriority(int mPriority) {
        this.mScrollUpPriority = mPriority;
    }

    public MultiNestedScrollingHelper getHelper() {
        return mHelper;
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return true;
    }

    public int getScrollDownPriority() {
        return mScrollDownPriority;
    }

    public void setScrollDownPriority(int scrollDownPriority) {
        mScrollDownPriority = scrollDownPriority;
    }
}
