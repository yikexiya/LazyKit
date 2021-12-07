package com.yikexiya.lazykit.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.yikexiya.lazykit.util.FunKit
import com.yikexiya.lazykit.util.dp
import com.yikexiya.lazykit.util.sp

class SingleInput(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14.sp
        strokeWidth = 2.dp
    }
    var text = ""
        set(value) {
            field = value
            invalidate()
        }
    var hint = ""
        set(value) {
            field = value
            invalidate()
        }
    var colorStates: ColorStateList = ColorStateList.valueOf(Color.BLACK)
        set(value) {
            field = value
            invalidate()
        }
    private val inputPadding = 12.dp
    private val radius = 5.dp
    private val cursorRunnable = Runnable {
        showCursor = !showCursor
        invalidate()
    }
    private var showCursor = false
    private val fontMetrics = Paint.FontMetrics()
    private val focusState = intArrayOf(android.R.attr.state_focused)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }
    private val inputMethodManager: InputMethodManager = context.getSystemService(InputMethodManager::class.java)!!

    override fun onDraw(canvas: Canvas) {
        // draw border
        paint.style = Paint.Style.STROKE
        paint.color = colorStates.getColorForState(drawableState, 0)
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
        // draw text
        paint.color = if (text.isEmpty())
            Color.parseColor("#FF3C3939")
        else
            colorStates.getColorForState(drawableState, 0)
        paint.style = Paint.Style.FILL
        paint.getFontMetrics(fontMetrics)
        val string = if (text.isEmpty()) hint else text
        canvas.drawText(string, inputPadding, FunKit.getCenterY(fontMetrics, height), paint)
        // draw cursor
        if (isFocused) {
            if (showCursor) {
                paint.style = Paint.Style.STROKE
                paint.color = colorStates.getColorForState(focusState, 0)
                val textHeight = fontMetrics.bottom - fontMetrics.top
                val stringX = if (text.isEmpty()) inputPadding else paint.measureText(string) + paint.fontSpacing
                canvas.drawLine(stringX, (height - textHeight) / 2f, stringX, (height + textHeight) / 2f, paint)
            }
            removeCallbacks(cursorRunnable)
            postDelayed(cursorRunnable, 500)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
            }
            MotionEvent.ACTION_UP -> {
                inputMethodManager.showSoftInput(this, 0)
                inputMethodManager.restartInput(this)
            }
        }
        return true
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    private val inputConnection by lazy {
        object : BaseInputConnection(this, true) {
            override fun beginBatchEdit(): Boolean {
//                Log.d("WSW", "beginBatchEdit: " + editable?.toString())
                return super.beginBatchEdit()
            }

            override fun endBatchEdit(): Boolean {
                this@SingleInput.text = editable?.toString() ?: ""
//                Log.d("WSW", "endBatchEdit: " + editable?.toString())
                return super.endBatchEdit()
            }
            override fun sendKeyEvent(event: KeyEvent): Boolean {
//                Log.d("WSW", "sendKeyEvent: $event")
                if (event.action == KeyEvent.ACTION_UP) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_ENTER -> inputMethodManager.hideSoftInputFromWindow(this@SingleInput.windowToken, 0)
//                        KeyEvent.KEYCODE_DEL -> text = text.dropLast(1)
                    }
                }
                return super.sendKeyEvent(event)
            }

            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
//                Log.d("WSW", "commitText: $text")
//                this@SingleInput.text = "${this@SingleInput.text}$text"
                return super.commitText(text, newCursorPosition)
            }
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
        return inputConnection
    }

}