package com.yikexiya.lazykit.ui.autoclick

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.app.MainApplication
import com.yikexiya.lazykit.util.PermissionExt
import com.yikexiya.lazykit.util.dp
import com.yikexiya.lazykit.util.toast
import kotlin.math.sqrt

class FingerView(context: Context) : View(context) {
    private val statusHeight: Int
    private val windowMaxWidth: Int
    private val windowMaxHeight: Int
    private val windowManager by lazy(LazyThreadSafetyMode.NONE) { context.getSystemService(WindowManager::class.java) }


    private val icon by lazy(LazyThreadSafetyMode.NONE) {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_finger) ?: throw RuntimeException("can't find bitmap")
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        statusHeight = if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
        val displayMetrics = Resources.getSystem().displayMetrics
        windowMaxWidth = displayMetrics.widthPixels
        windowMaxHeight = displayMetrics.heightPixels
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(icon, 0f, 0f, paint)
    }

    private var moved = false
    private var downX = 0f
    private var downY = 0f
    private var wmLpDownX = 0
    private var wmLpDownY = 0
    private val moveSlop = 8f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                moved = false
                downX = x
                downY = y
                wmLpDownX = wmLp.x.coerceIn(0, windowMaxWidth - width)
                wmLpDownY = wmLp.y.coerceIn(0, windowMaxHeight)
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = x - downX
                val offsetY = y - downY
                if (sqrt(offsetX * offsetX + offsetY * offsetY) > moveSlop)
                    moved = true
                wmLp.x = (wmLpDownX + offsetX).toInt()
                wmLp.y = (wmLpDownY + offsetY).toInt()
                windowManager.updateViewLayout(this, wmLp)
            }
            MotionEvent.ACTION_UP -> {
                if (!moved) {
                    if (PermissionExt.hasPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)) {
                        click(x, y)
                    } else {
                        PermissionExt.requestPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)
                    }
                }
            }
        }
        return true
    }

    private fun click(x: Float, y: Float) {
        if (MainApplication.instance().addClickPoint(x, y)) {
            windowManager.removeView(this)
            AutoClickService.click(context, x, y)
        } else {
            toast("现在每10个点必须保存")
        }
    }

    fun show() {
        windowManager.addView(this, wmLp)
    }

    fun remove() {
        windowManager.removeView(this)
    }

    companion object {
        private val wmLp = WindowManager.LayoutParams(
                48.dp.toInt(),
                48.dp.toInt(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }
}