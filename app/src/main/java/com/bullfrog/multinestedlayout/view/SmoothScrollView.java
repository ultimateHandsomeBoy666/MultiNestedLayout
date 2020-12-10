package com.bullfrog.multinestedlayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bullfrog.multinestedlayout.R;


public class SmoothScrollView extends ScrollView implements NestedScrollingParent2 {

    private int mTopHeight;
    private NestedScrollingParentHelper mHelper = new NestedScrollingParentHelper(this);
    private RecyclerView mRvChild;

    public SmoothScrollView(Context context) {
        super(context);
    }

    public SmoothScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmoothScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mHelper.onStopNestedScroll(target, type);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
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
        Log.d("onNestedPreScroll", "cur view is " + getId());
        boolean hideTop = getScrollY() < mTopHeight && dy > 0;
        boolean showTop = (getScrollY() > mTopHeight || !mRvChild.canScrollVertically(-1)) && dy < 0;
        if (hideTop || showTop) {
            scrollBy(0, dy);
            consumed[1] = dy;
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mHelper.getNestedScrollAxes();
    }

//    @Override
//    public void scrollTo(int x, int y) {
//        if (y < 0) {
//            y = 0;
//        }
//        if (y > mTopHeight) {
//            y = mTopHeight;
//        }
//        if (getScrollY() != y) {
//            super.scrollTo(x, y);
//        }
//    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRvChild = findViewById(R.id.rv);
    }

    public int getTopHeight() {
        return mTopHeight;
    }

    public void setTopHeight(int mTopHeight) {
        this.mTopHeight = mTopHeight;
    }

    public NestedScrollingParentHelper getHelper() {
        return mHelper;
    }

    public void setHelper(NestedScrollingParentHelper mHelper) {
        this.mHelper = mHelper;
    }
}
