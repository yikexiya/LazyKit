package com.yikexiya.lazykit.util

import android.graphics.Paint

/**
 * 计算文字y坐标根据指定的y坐标
 */
fun Paint.calcFontYByY(y: Int): Float {
    return y - getFontHeightCenter()
}

/**
 * 获取字体高度的中间值
 */
fun Paint.getFontHeightCenter(): Float {
    return (fontMetrics.bottom + fontMetrics.top) * 0.5f
}