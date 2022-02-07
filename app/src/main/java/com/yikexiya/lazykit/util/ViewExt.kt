package com.yikexiya.lazykit.util

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

private val metrics = Resources.getSystem().displayMetrics
val Int.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics)
val Int.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), metrics)

fun View.layout(left: Int, top: Int) {
    layout(left, top, left + measuredWidth, top + measuredHeight)
}
fun View.centerHorizontal(source: View, top: Int, isRelateSource: Boolean = false) {
    val left = (if (parent == source) 0 else source.left) + (source.measuredWidth - measuredWidth) / 2
    val t = (if (isRelateSource) source.top else 0) + top
    layout(left, t, left + measuredWidth, t + measuredHeight)
}
fun View.centerVertical(source: View, left: Int, isRelateSource: Boolean = false) {
    val l = (if (isRelateSource) source.left else 0) + left
    val top = (if (parent == source) 0 else source.top) + (source.measuredHeight - measuredHeight) / 2
    layout(l, top, l + measuredWidth, top + measuredHeight)
}
fun View.centerView(source: View) {
    val left = (if (parent == source) 0 else source.left) + (source.measuredWidth - measuredWidth) / 2
    val top = (if (parent == source) 0 else source.top) + (source.measuredHeight - measuredHeight) / 2
    layout(left, top, left + measuredWidth, top + measuredHeight)
}
fun ViewGroup.constraintChildrenWith(distance: Int, children: List<View>) {
    val space = (measuredWidth - children.sumOf { it.measuredWidth }) / (children.size + 1)
    var left = space
    children.forEach {
        it.layout(left, distance, left + it.measuredWidth, distance + it.measuredHeight)
        left += it.measuredWidth + space
    }
}

fun View.showAsDialog() {
    val windowManager = context.getSystemService(WindowManager::class.java)
    if (parent != null) {
        throw RuntimeException("现在还不支持添加到ViewGroup上面的View")
        return
    }
//    windowManager.addView(this, null)
}

fun View.autoMeasure(parent: ViewGroup) {
    measure(defaultWidthMeasureSpec(parent), defaultHeightMeasureSpec(parent))
}

private fun View.defaultWidthMeasureSpec(parent: ViewGroup): Int {
    return when (layoutParams.width) {
        ViewGroup.LayoutParams.MATCH_PARENT -> parent.measuredWidth.toExactlyMeasureSpec()
        ViewGroup.LayoutParams.WRAP_CONTENT -> parent.measuredWidth.toAtMostMeasureSpec()
        0 -> throw IllegalAccessException("Need special treatment for $this")
        else -> layoutParams.width.toExactlyMeasureSpec()
    }
}

private fun View.defaultHeightMeasureSpec(parent: ViewGroup): Int {
    return when (layoutParams.height) {
        ViewGroup.LayoutParams.MATCH_PARENT -> parent.measuredHeight.toExactlyMeasureSpec()
        ViewGroup.LayoutParams.WRAP_CONTENT -> parent.measuredHeight.toAtMostMeasureSpec()
        0 -> throw IllegalAccessException("Need special treatment for $this")
        else -> layoutParams.height.toExactlyMeasureSpec()
    }
}

private fun Int.toExactlyMeasureSpec(): Int {
    return View.MeasureSpec.makeMeasureSpec(this, View.MeasureSpec.EXACTLY)
}

private fun Int.toAtMostMeasureSpec(): Int {
    return View.MeasureSpec.makeMeasureSpec(this, View.MeasureSpec.AT_MOST)
}