package com.bullfrog.multinestedlayout.view;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewParentCompat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiNestedScrollingHelper {

    private int[] mTempNestedScrollConsumed;
    private final ViewGroup mMultiNestedScrollView;
    // mTopHeight 应该在 onSizeChanged 里面进行计算
    private int mTopHeight;

    public MultiNestedScrollingHelper(ViewGroup viewGroup) {
        mMultiNestedScrollView = viewGroup;
    }

    public boolean startNestedScroll(@ViewCompat.ScrollAxis int axes, @ViewCompat.NestedScrollType int type) {
        if (MultiNestedPriorityHelper.getInstance().scrollUpPriorityMapSize() != 0 &&
            MultiNestedPriorityHelper.getInstance().scrollDownPriorityMapSize() != 0) {
            return true;
        }
        if (isNestedScrollEnabled()) {
            ViewParent p = mMultiNestedScrollView;
            View child = mMultiNestedScrollView;
            while (p != null) {
                if (p instanceof MultiNestedScrollView &&
                        ViewParentCompat.onStartNestedScroll(p, child, mMultiNestedScrollView, axes, type)) {
                    ViewParentCompat.onNestedScrollAccepted(p, child, mMultiNestedScrollView, axes, type);
                    return true;
                }
                if (p instanceof View) {
                    child = (View) p;
                }
                p = p.getParent();
            }
        }
        return false;
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, @Nullable int[] offsetInWindow,
                                        @ViewCompat.NestedScrollType int type) {
        return dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, null);
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, @Nullable int[] offsetInWindow,
                                        @ViewCompat.NestedScrollType int type,
                                        @Nullable int[] consumed) {
        return dispatchNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    public boolean dispatchNestedScrollInternal(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                                int dyUnconsumed, @Nullable int[] offsetInWindow,
                                                @ViewCompat.NestedScrollType int type,
                                                @Nullable int[] consumed) {
        int maxPriority = mPriorityParentMap.size();
        if (isNestedScrollEnabled() && maxPriority != 0) {
            ViewParent p = mPriorityParentMap.get(maxPriority);
            if (p == null) {
                return false;
            }
            if (dxConsumed != 0 || dyConsumed != 0 || dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    mMultiNestedScrollView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }
                if (consumed == null) {
                    consumed = getTempNestedScrollConsumed();
                }

                ViewParentCompat.onNestedScroll(p, mMultiNestedScrollView, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);

                if (offsetInWindow != null) {
                    mMultiNestedScrollView.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
                                           @Nullable int[] offsetInWindow,
                                           @ViewCompat.NestedScrollType int type) {
        if (isNestedScrollEnabled()) {
            ViewParent p;
            if (dy > 0) {
                p = MultiNestedPriorityHelper.getInstance().pollScrollUpPriority(mMultiNestedScrollView);
            } else {
                p = MultiNestedPriorityHelper.getInstance().pollScrollDownPriority(mMultiNestedScrollView);
            }
            if (p == null) {
                return false;
            }
            if (dx != 0 || dy != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    mMultiNestedScrollView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }
                if (consumed == null) {
                    consumed = getTempNestedScrollConsumed();
                }

                ViewParentCompat.onNestedPreScroll(p, mMultiNestedScrollView, dx, dy, consumed, type);

                if (offsetInWindow != null) {
                    mMultiNestedScrollView.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                return consumed[0] != 0 || consumed[1] != 0;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public void stopNestedScroll(int type) {
        ViewParent p = mMultiNestedScrollView.getParent();
        while (p != null) {
            if (p instanceof MultiNestedScrollView) {
                ViewParentCompat.onStopNestedScroll(p, mMultiNestedScrollView, type);
            }
            p = p.getParent();
        }
    }

    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes,
                                @ViewCompat.NestedScrollType int type) {
//        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        return true;
    }

    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes,
                                       @ViewCompat.NestedScrollType int type) {
        // here to offer multiNestedScrollViews into priority queue, recursively
        if (isNestedScrollEnabled()) {
            ViewParent p = mMultiNestedScrollView.getParent();
            View curChild = mMultiNestedScrollView;
            while (p != null) {
                if (p instanceof MultiNestedScrollView &&
                        ViewParentCompat.onStartNestedScroll(p, curChild, target, axes, type)) {
                    MultiNestedPriorityHelper.getInstance().offerScrollUpPriority((MultiNestedScrollView) p);
                    MultiNestedPriorityHelper.getInstance().offerScrollDownPriority((MultiNestedScrollView) p);
                    ViewParentCompat.onNestedScrollAccepted(p, curChild, target, axes, type);
                    break;
                }
                if (p instanceof View) {
                    curChild = (View) p;
                }
                p = p.getParent();
            }
        }
    }

    public int getNestedScrollAxes() {
        return ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                        int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
    }

    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed,
                                  @ViewCompat.NestedScrollType int type) {
//        Log.d("onNestedPreScroll multi", "cur view is " + mMultiNestedScrollView.getId() + "" +
//                " dy = " + dy + ", topHeight = " + mTopHeight + ", scrollY = " + mMultiNestedScrollView.getScrollY());
//        boolean hideTop = mMultiNestedScrollView.getScrollY() < mTopHeight && dy > 0;
//        boolean showTop = mMultiNestedScrollView.getScrollY() > 0 && dy < 0;
////        dispatchNestedPreScroll(dx, dy, consumed, null, type);
//        if ((hideTop || showTop) && isNestedScrollEnabled()) {
//            Log.d("onNestedPreScroll multi", "dy - consumed[1] = " + (dy - consumed[1]));
//            mMultiNestedScrollView.scrollBy(0, dy - consumed[1]);
//            consumed[1] = dy;
//        }

        // NestedScroll triggers here

    }

    public void onNestedPreScrollActual(int dx, int dy, @NonNull int[] consumed) {
        Log.d("onNestedPreScroll multi", "cur view is " + mMultiNestedScrollView.getId() + "" +
                " dy = " + dy + ", topHeight = " + mTopHeight + ", scrollY = " + mMultiNestedScrollView.getScrollY());
        boolean hideTop = mMultiNestedScrollView.getScrollY() < mTopHeight && dy > 0;
        boolean showTop = mMultiNestedScrollView.getScrollY() > 0 && dy < 0;
        if ((hideTop || showTop) && isNestedScrollEnabled()) {
            Log.d("onNestedPreScroll multi", "dy - consumed[1] = " + (dy - consumed[1]));
            mMultiNestedScrollView.scrollBy(0, dy);
            consumed[1] += dy;
        }
    }

    public void onStopNestedScroll(@NonNull View target, @ViewCompat.NestedScrollType int type) {
//        Iterator<Map.Entry<Integer, ViewParent>> iterator = mPriorityParentMap.entrySet().iterator();
//        Log.d("onStopNestedScroll", "map size = " + mPriorityParentMap.size() + ", has next = " + iterator.hasNext());
//        while (iterator.hasNext()) {
//            ViewParent p = iterator.next().getValue();
//            ViewParentCompat.onStopNestedScroll(p, target, type);
//            iterator.remove();
//        }
    }

    public boolean isNestedScrollEnabled() {
        return mMultiNestedScrollView.isNestedScrollingEnabled();
    }

    public void setNestedScrollEnabled(boolean enable) {
        mMultiNestedScrollView.setNestedScrollingEnabled(enable);
    }

    private int[] getTempNestedScrollConsumed() {
        if (mTempNestedScrollConsumed == null) {
            mTempNestedScrollConsumed = new int[]{0, 0};
        }
        return mTempNestedScrollConsumed;
    }

    public int getTopHeight() {
        return mTopHeight;
    }

    public void setTopHeight(int topHeight) {
        mTopHeight = topHeight;
    }

    public ViewGroup getMultiNestedScrollView() {
        return mMultiNestedScrollView;
    }
}
