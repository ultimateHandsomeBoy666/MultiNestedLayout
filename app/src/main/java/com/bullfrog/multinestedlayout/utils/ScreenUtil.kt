package com.bullfrog.multinestedlayout.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics

fun getScreenHeight(context: Context): Int = context.resources?.displayMetrics?.heightPixels ?: 0

fun getScreenWidth(context: Context): Int = context.resources?.displayMetrics?.widthPixels ?: 0

fun getStatusBarHeight(context: Context): Int {
    val res = context.resources
    val resId = res.getIdentifier("status_bar_height", "dimen", "android")
    return if (resId > 0) res.getDimensionPixelSize(resId) else 0
}

fun getDisplayHeight(activity: Activity): Int {
    val display = activity.display
    val point = Point()
    display?.let {
        it.getSize(point)
        return point.y
    }
    return 0
}