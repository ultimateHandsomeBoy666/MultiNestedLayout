package com.bullfrog.multinestedlayout.utils

import com.bullfrog.multinestedlayout.MainApplication

fun getScreenHeight(): Int = MainApplication.INSTANCE.resources?.displayMetrics?.heightPixels ?: 0

fun getScreenWidth(): Int = MainApplication.INSTANCE.resources?.displayMetrics?.widthPixels ?: 0

fun getStatusBarHeight(): Int {
    val res = MainApplication.INSTANCE.resources
    val resId = res.getIdentifier("status_bar_height", "dimen", "android")
    return if (resId > 0) res.getDimensionPixelSize(resId) else 0
}
