package com.yikexiya.lazykit.ui.autoclick

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.children
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.util.*

@SuppressLint("ClickableViewAccessibility")
class DirectionView(context: Context) : ViewGroup(context) {
    private val iconLayoutParams = LayoutParams(50.dp.toInt(), 50.dp.toInt())
    private val dColor = Color.parseColor("#CC45AADD")
    private val leftIcon = imageView(R.drawable.icon_direction_left)
    private val topIcon = imageView(R.drawable.icon_direction_up)
    private val rightIcon = imageView(R.drawable.icon_direction_right)
    private val bottomIcon = imageView(R.drawable.icon_direction_down)
    private val sureIcon = AppCompatImageView(context).apply {
        layoutParams = iconLayoutParams
        this@DirectionView.addView(this)
        setImageResource(R.drawable.icon_drag)
        setBackgroundColor(Color.WHITE)
    }
    private val doneIcon = View(context).apply {
        layoutParams = iconLayoutParams
        this@DirectionView.addView(this)
        setBackgroundColor(Color.BLUE)
    }
    private val longPressRunnable = object : Runnable {
        var xOffset = 0f
        var yOffset = 0f
        override fun run() {
            locationChangeEvent?.invoke(xOffset, yOffset)
            this@DirectionView.postDelayed(this, 50)
        }
    }
    private val directionTouchListener: OnTouchListener = object : OnTouchListener {
        private var moveSlop = 12
        private val downPoint = PointF()
        private var stateClick = true
        private var stateNoDeal = false

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downPoint.set(x, y)
                    val xOffset: Float
                    val yOffset: Float
                    when (v) {
                        leftIcon -> {
                            xOffset = -10f
                            yOffset = 0f
                        }
                        topIcon -> {
                            xOffset = 0f
                            yOffset = -10f
                        }
                        rightIcon -> {
                            xOffset = 10f
                            yOffset = 0f
                        }
                        bottomIcon -> {
                            xOffset = 0f
                            yOffset = 10f
                        }
                        else -> throw RuntimeException("no")
                    }
                    longPressRunnable.xOffset = xOffset
                    longPressRunnable.yOffset = yOffset
                    this@DirectionView.postDelayed(longPressRunnable, 600)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (stateNoDeal)
                        return true
                    if (x < 0 || x > v.width || y < 0 || y > v.height)
                        noDeal()
                }
                MotionEvent.ACTION_UP -> {
                    if (stateClick) {
                        val xOffset: Float
                        val yOffset: Float
                        when (v) {
                            leftIcon -> {
                                xOffset = -5f
                                yOffset = 0f
                            }
                            topIcon -> {
                                xOffset = 0f
                                yOffset = -5f
                            }
                            rightIcon -> {
                                xOffset = 5f
                                yOffset = 0f
                            }
                            bottomIcon -> {
                                xOffset = 0f
                                yOffset = 5f
                            }
                            else -> throw RuntimeException("no")
                        }
                        locationChangeEvent?.invoke(xOffset, yOffset)
                    }
                    clearState()
                }
                MotionEvent.ACTION_CANCEL -> {
                    clearState()
                }
                else -> {
                    if (stateNoDeal)
                        return true
                    noDeal()
                }
            }
            return true
        }

        private fun clearState() {
            this@DirectionView.removeCallbacks(longPressRunnable)
            stateClick = true
            stateNoDeal = false
        }
        private fun noDeal() {
            this@DirectionView.removeCallbacks(longPressRunnable)
            stateNoDeal = true
        }
    }

    override fun shouldDelayChildPressedState() = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        children.forEach { it.autoMeasure(this) }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        sureIcon.centerHorizontal(this, iconLayoutParams.width)
        leftIcon.layout(sureIcon.left - iconLayoutParams.width, sureIcon.top)
        topIcon.layout(sureIcon.left, sureIcon.top - iconLayoutParams.height)
        rightIcon.layout(sureIcon.right, sureIcon.top)
        bottomIcon.layout(sureIcon.left, sureIcon.bottom)
        doneIcon.layout(sureIcon.left, bottomIcon.bottom)
    }

    var locationChangeEvent: ((x: Float, y: Float) -> Unit)? = null
    fun setSureIconTouchEvent(touchListener: OnTouchListener) {
        sureIcon.setOnTouchListener(touchListener)
    }
    fun setDoneEvent(clickEvent: OnClickListener, doneEvent: OnLongClickListener) {
        doneIcon.setOnClickListener(clickEvent)
        doneIcon.setOnLongClickListener(doneEvent)
    }

    private fun imageView(iconRes: Int): ImageView {
        return object : AppCompatImageView(context){
            override fun onTouchEvent(event: MotionEvent): Boolean {
                return directionTouchListener.onTouch(this, event)
            }
        }.apply {
            layoutParams = iconLayoutParams
            this@DirectionView.addView(this)
            setImageResource(iconRes)
            setBackgroundColor(dColor)
        }
    }
}