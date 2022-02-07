package com.yikexiya.lazykit.util

import android.graphics.Paint

object FunKit {
    /**
     * 获取在指定高度下能够使文字垂直居中的高度值
     * @param fontMetrics
     * @param height 指定的高度
     * @return 指定高度居中的y值
     */
    fun getCenterY(fontMetrics: Paint.FontMetrics, height: Int): Float {
        return (height - fontMetrics.bottom - fontMetrics.top) / 2f
    }

    /**
     * 计算文字y坐标根据指定的y坐标
     */
    fun calcFontYByY(fontMetrics: Paint.FontMetrics, y: Int): Float {
        return y - (fontMetrics.bottom + fontMetrics.top) * 0.5f
    }
}