package com.yikexiya.lazykit.theme

import android.view.ViewGroup
import com.yikexiya.lazykit.util.dp

object SizeKit {
    val matchParent = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    val wrapContent = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val ignoreLp = ViewGroup.LayoutParams(0, 0)
    val iconSize = ViewGroup.LayoutParams(32.dp.toInt(), 32.dp.toInt())
    val lineLayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ThemeKit.lineHeight.toInt())
}