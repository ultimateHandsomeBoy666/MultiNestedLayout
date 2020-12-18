package com.bullfrog.multinestedlayout.view;

import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

final class MultiNestedPriorityManager {

    // the map key here is the layout root view of the MultiNestedScrollView
    private Map<View, PriorityQueue<MultiNestedScrollView>> scrollUpPriorityMap = new HashMap<>();
    private Map<View, PriorityQueue<MultiNestedScrollView>> scrollDownPriorityMap = new HashMap<>();

    // cache map for view to get its root view
    private Map<View, View> cacheRootMap = new HashMap<>();

    private static MultiNestedPriorityManager mInstance;

    public static MultiNestedPriorityManager getInstance() {
        if (mInstance == null) {
            mInstance = new MultiNestedPriorityManager();
        }
        return mInstance;
    }

    public static void offerScrollUpPriority(MultiNestedScrollView view) {
        if (mInstance.cacheRootMap)
        View root = view.getRootView();
        if (!mInstance.scrollUpPriorityMap.containsKey(root)) {
            mInstance.scrollUpPriorityMap.put(root, new PriorityQueue<>());
        }
        PriorityQueue<MultiNestedScrollView> queue = mInstance.scrollUpPriorityMap.get(root);
        if (!queue.contains(view)) {
            queue.offer(view);
        }
    }

    public static void pollScrollUpPriority(MultiNestedScrollView view) {
        View root = view.getRootView();
        if (!mInstance.scrollUpPriorityMap.containsKey(root)) {
            return;
        }
        PriorityQueue<MultiNestedScrollView> queue = mInstance.scrollUpPriorityMap.get(root);
        if (!queue.contains(view)) {
            queue.offer(view);
        }
    }
}
