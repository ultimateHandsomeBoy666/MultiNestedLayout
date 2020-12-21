package com.bullfrog.multinestedlayout.view;

import android.view.View;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

final class MultiNestedPriorityHelper {

    // the map key here is the layout root view of the MultiNestedScrollView
    private Map<View, PriorityQueue<MultiNestedScrollView>> scrollUpPriorityMap = new HashMap<>();
    private Map<View, PriorityQueue<MultiNestedScrollView>> scrollDownPriorityMap = new HashMap<>();

    // cache map for view to get its root view
    private Map<View, View> cacheRootMap = new HashMap<>();

    private static MultiNestedPriorityHelper mInstance;

    public static MultiNestedPriorityHelper getInstance() {
        if (mInstance == null) {
            mInstance = new MultiNestedPriorityHelper();
        }
        return mInstance;
    }

    public int scrollUpPriorityMapSize() {
        return scrollUpPriorityMap.size();
    }

    public int scrollDownPriorityMapSize() {
        return scrollDownPriorityMap.size();
    }

    public void offerScrollUpPriority(MultiNestedScrollView view) {
        View root;
        if (!cacheRootMap.containsKey(view)) {
            root = view.getRootView();
            cacheRootMap.put(view, root);
        } else {
            root = cacheRootMap.get(view);
        }
        if (!scrollUpPriorityMap.containsKey(root)) {
            scrollUpPriorityMap.put(root, new PriorityQueue<>(6, (nsv1, nsv2) -> {
                if (nsv1.getScrollDownPriority() >= nsv2.getScrollDownPriority()) {
                    return 1;
                } else {
                    return -1;
                }
            }));
        }
        PriorityQueue<MultiNestedScrollView> queue = scrollUpPriorityMap.get(root);
        if (!queue.contains(view)) {
            queue.offer(view);
        }
    }

    public MultiNestedScrollView pollScrollUpPriority(View view) {
        View root;
        if (!cacheRootMap.containsKey(view)) {
            root = view.getRootView();
            cacheRootMap.put(view, root);
        } else {
            root = cacheRootMap.get(view);
        }
        if (!scrollUpPriorityMap.containsKey(root)) {
            return null;
        }
        PriorityQueue<MultiNestedScrollView> queue = scrollUpPriorityMap.get(root);
        return queue.poll();
    }

    public void offerScrollDownPriority(MultiNestedScrollView view) {
        View root;
        if (!cacheRootMap.containsKey(view)) {
            root = view.getRootView();
            cacheRootMap.put(view, root);
        } else {
            root = cacheRootMap.get(view);
        }
        if (!scrollDownPriorityMap.containsKey(root)) {
            scrollDownPriorityMap.put(root, new PriorityQueue<>(6, (nsv1, nsv2) -> {
                if (nsv1.getScrollDownPriority() >= nsv2.getScrollDownPriority()) {
                    return 1;
                } else {
                    return -1;
                }
            }));
        }
        PriorityQueue<MultiNestedScrollView> queue = scrollDownPriorityMap.get(root);
        if (!queue.contains(view)) {
            queue.offer(view);
        }
    }

    public MultiNestedScrollView pollScrollDownPriority(View view) {
        View root;
        if (!cacheRootMap.containsKey(view)) {
            root = view.getRootView();
            cacheRootMap.put(view, root);
        } else {
            root = cacheRootMap.get(view);
        }
        if (!scrollDownPriorityMap.containsKey(root)) {
            return null;
        }
        PriorityQueue<MultiNestedScrollView> queue = scrollDownPriorityMap.get(root);
        return queue.poll();
    }

    public void dispatchNestedPreScrollByPriority(@NonNull View target, int dx, int dy, @NonNull int[] consumed,
                                                  @ViewCompat.NestedScrollType int type) {
        if (dy > 0) {
            while (scrollUpPriorityMapSize() > 0) {
                MultiNestedScrollView view = pollScrollUpPriority(target);
                view.onNestedScA(target, dx, dy, consumed, type);
            }
        } else if (dy < 0) {
            MultiNestedScrollView view = pollScrollDownPriority(target);
            view.onNestedPreScroll(target, dx, dy, consumed, type);
        }

    }
}
