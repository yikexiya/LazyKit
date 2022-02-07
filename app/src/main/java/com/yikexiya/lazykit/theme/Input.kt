package com.yikexiya.lazykit.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import com.yikexiya.lazykit.util.layout
import com.yikexiya.lazykit.view.WheelView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val inputLayout = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

fun input(
    parent: ViewGroup,
    placeHolder: String,
    lp: ViewGroup.LayoutParams = inputLayout
) : EditText = AppCompatEditText(parent.context).apply {
    layoutParams = lp
    hint = placeHolder
    parent.addView(this)
}

@SuppressLint("ViewConstructor")
class TimeInput(context: Context, private val secondSelected: (second: Long?) -> Unit) : ViewGroup(context) {
    private val wheelView = WheelView(context).apply {
        layoutParams = SizeKit.ignoreLp
        this@TimeInput.addView(this)
    }
    private val sure = button(this, "确定", {
        val selectedIndexes = wheelView.getSelectedIndexes()
        val secondInDay = selectedIndexes[0] * 3600 + selectedIndexes[1] * 60 + selectedIndexes[2]
        dismiss(secondInDay.toLong())
    }, SizeKit.ignoreLp)
    private val cancel = button(this, "取消", {
        dismiss(null)
    }, SizeKit.ignoreLp)

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val windowWidth = MeasureSpec.getSize(widthMeasureSpec)
        val buttonWidthSpec = MeasureSpec.makeMeasureSpec(windowWidth / 2, MeasureSpec.EXACTLY)
        val buttonHeightSpec = MeasureSpec.makeMeasureSpec(buttonHeight, MeasureSpec.EXACTLY)
        sure.measure(buttonWidthSpec, buttonHeightSpec)
        cancel.measure(buttonWidthSpec, buttonHeightSpec)
        val wheelHeight = MeasureSpec.getSize(heightMeasureSpec) - buttonHeight
        wheelView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(wheelHeight, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        wheelView.layout(0, 0)
        sure.layout(wheelView.left, wheelView.bottom)
        cancel.layout(sure.right, sure.top)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
    }

    fun show() {
        showAsBottomDialog()
    }

    fun dismiss(secondInDay: Long?) {
        secondSelected(secondInDay)
        dismissAsDialog()
    }

    fun setSecondInDay(second: Long) {
        val wheelColumns = WheelView.WheelColumn.parseSecondInDay(second)
        wheelView.setWheelColumns(wheelColumns)
    }

    companion object {
        private val buttonHeight = ThemeKit.lineHeight.toInt()

        /**
         * 获取一天内的时间，已秒为单位
         */
        suspend fun getTimeSecondInDay(context: Context, secondPlaceholder: Long = 0) = suspendCoroutine<Long?> { continuation ->
            val timeInput = TimeInput(context) { second -> continuation.resume(second) }
            timeInput.setSecondInDay(secondPlaceholder)
            timeInput.show()
        }
    }
}