package com.yikexiya.lazykit.theme

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.util.dp
import com.yikexiya.lazykit.view.SingleInput

object ThemeKit {
    val sidePadding = 20.dp
    val sidePaddingMiddle = 16.dp
    val linePadding = 10.dp
    val lineHeight = 44.dp
    val cardRadius = 8.dp

    private var theme: Int = R.style.Theme_LazyKit

    fun getTheme(): Int {
        return R.style.Theme_LazyKit
    }

    fun changeTheme(context: Context) {
        if (context is Activity)
            context.recreate()
    }

    private val inputColors: ColorStateList by lazy {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf()
        )
        val colors = intArrayOf(
            Color.RED,
            Color.RED,
            Color.GREEN,
            Color.GRAY,
        )
        ColorStateList(states, colors)
    }
    fun input(
        parent: ViewGroup,
        hint: String,
        lp: ViewGroup.LayoutParams = SizeKit.lineLayoutParams
    ) = SingleInput(parent.context).apply {
        layoutParams = lp
        parent.addView(this)
        this.hint = hint
        colorStates = inputColors
    }
}