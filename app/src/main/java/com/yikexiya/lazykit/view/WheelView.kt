package com.yikexiya.lazykit.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.yikexiya.lazykit.util.dp
import com.yikexiya.lazykit.util.getFontHeightCenter
import com.yikexiya.lazykit.util.sp
import kotlin.math.roundToInt

// TODO: 还可以增加标题栏和item列之间的装饰
class WheelView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22.sp
        textAlign = Paint.Align.CENTER
    }

    private val heightGap = 44.dp
    private val dataList = mutableListOf<WheelColumn>()
    private val flipAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 600
        interpolator = DecelerateInterpolator()
        addUpdateListener { animator ->
            movedWheelColumn.indexF = animator.animatedValue as Float
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (dataList.isEmpty())
            return
        val eachWidth = width / dataList.size
        dataList.forEachIndexed { index, wheelColumn ->
            val startX = (index + 0.5f) * eachWidth
            wheelColumn.draw(canvas, startX, height, heightGap, paint)
        }
    }

    private lateinit var velocityTracker: VelocityTracker
    private lateinit var movedWheelColumn: WheelColumn
    private var lastDownY = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (dataList.isEmpty())
            return true
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                flipAnimator.cancel()
                val x = event.x
                val eachWidth = width / dataList.size
                val columnIndex = ((x / eachWidth).toInt()).coerceAtMost(dataList.size - 1)
                movedWheelColumn = dataList[columnIndex]
                lastDownY = y
                velocityTracker.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetY = y - lastDownY
                lastDownY = y
                movedWheelColumn.indexF -= (offsetY / heightGap)
                invalidate()
                velocityTracker.addMovement(event)
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker.addMovement(event)
                velocityTracker.computeCurrentVelocity(200, 500f)
                val yVelocity = velocityTracker.yVelocity
                val flipStart = movedWheelColumn.indexF
                val flipEnd = (flipStart - yVelocity / heightGap).roundToInt().toFloat()
                flipAnimator.setFloatValues(flipStart, flipEnd)
                flipAnimator.start()
                velocityTracker.clear()
            }
        }
        return true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        velocityTracker = VelocityTracker.obtain()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        velocityTracker.recycle()
    }

    fun setWheelColumns(wheelColumns: List<WheelColumn>) {
        dataList.clear()
        dataList.addAll(wheelColumns)
    }

    fun getSelectedIndexes(): List<Int> {
        return dataList.map { it.indexF.toInt() }
    }

    class WheelColumn(private val textList: List<String>) {
        /**
         * index的值整数部分表示数据的索引，对于绘制永远指向中间位置，范围在[0, testList.size)
         */
        var indexF = 0f
            set(value) {
                field = value.coerceIn(0f, textList.size - 1f)
            }
        fun draw(canvas: Canvas, x: Float, height: Int, heightGap: Float, paint: Paint) {
            val originColor = paint.color
            var index = indexF.toInt()
            val offset = indexF - index
            val fontHeightCenter = paint.getFontHeightCenter()
            // 中间数据的中心线的y坐标
            val centerLine = height / 2f - offset * heightGap
            var currentHeight = centerLine
            // 绘制中间
            paint.color = Color.RED
            canvas.drawText(textList[index], x, currentHeight - fontHeightCenter, paint)
            currentHeight = centerLine + heightGap
            // 考虑到最上面和最下面会显示一般的这种情况需要显示，所以高度需要加一个间隔，最小需要减去一个间隔
            // 循环画和不循环画的区别主要在于当index不再数据列表范围内时，循环自动补上，不循环退出
            paint.color = originColor
            val maxIndex = textList.size
            // 向下绘制直到大于高度
            while (currentHeight < height + heightGap) {
                index++
                if (index >= maxIndex) {
                    break
                }
                canvas.drawText(textList[index], x, currentHeight - fontHeightCenter, paint)
                currentHeight += heightGap
            }
            index = indexF.toInt()
            currentHeight = centerLine - heightGap
            // 向上绘制直到小于0
            while (currentHeight > -heightGap) {
                index--
                if (index < 0) {
                    break
                }
                canvas.drawText(textList[index], x, currentHeight - fontHeightCenter, paint)
                currentHeight -= heightGap
            }
            paint.color = originColor
        }

        companion object {
            private val hourStr = listOf(
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23"
            )
            private val minuteAndSecondStr = listOf(
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                "50", "51", "52", "53", "54", "55", "56", "57", "58", "59",
            )
            fun parseSecondInDay(secondInDay: Long): List<WheelColumn> {
                val hour = secondInDay / 3600
                val minutes = secondInDay % 3600 / 60
                val second = secondInDay % 60
                val hourColumn = WheelColumn(hourStr)
                hourColumn.indexF = hour.toFloat()
                val minutesColumn = WheelColumn(minuteAndSecondStr)
                minutesColumn.indexF = minutes.toFloat()
                val secondColumn = WheelColumn(minuteAndSecondStr)
                secondColumn.indexF = second.toFloat()
                return listOf(hourColumn, minutesColumn, secondColumn)
            }
        }
    }

}