package com.yikexiya.lazykit.theme

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

fun text(
    parent: ViewGroup,
    text: String,
    lp: ViewGroup.LayoutParams = SizeKit.wrapContent
): TextView = MaterialTextView(parent.context).apply {
    layoutParams = lp
    this.text = text
    parent.addView(this)
}

fun button(
    parent: ViewGroup,
    text: String,
    onClick: View.OnClickListener,
    lp: ViewGroup.LayoutParams = SizeKit.wrapContent
): Button = MaterialButton(parent.context).apply {
    layoutParams = lp
    parent.addView(this)
    this.text = text
    setOnClickListener(onClick)
}


private val iconTintList = ColorStateList(
    arrayOf(
        intArrayOf(android.R.attr.state_pressed),
        intArrayOf()
    ),
    intArrayOf(
        Color.RED,
        Color.BLUE
    )
)

fun icon(
    parent: ViewGroup,
    resId: Int,
    colorList: ColorStateList? = iconTintList,
    lp: ViewGroup.LayoutParams = SizeKit.iconSize
): ImageView = AppCompatImageView(parent.context).apply {
    layoutParams = lp
    setImageResource(resId)
    imageTintList = colorList
    parent.addView(this)
}

fun dialog(context: Context): Dialog {
    return Dialog(context)
}