package com.yikexiya.lazykit.ui.autoclick

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PointF
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.view.*
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.yikexiya.lazykit.theme.SizeKit
import com.yikexiya.lazykit.util.dp
import com.yikexiya.lazykit.util.toast
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@SuppressLint("StaticFieldLeak")
object AttendManager {
    private val menuLayout = WindowManager.LayoutParams(
        150.dp.toInt(),
        200.dp.toInt(),
        100,
        100,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.RGBA_8888
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }
    private val pointLayout = WindowManager.LayoutParams(
        20.dp.toInt(),
        20.dp.toInt(),
        300,
        300,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.RGBA_8888
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }
    private lateinit var windowManager: WindowManager
    private lateinit var directionView: DirectionView
    private lateinit var pointView: View
    private var statusHeight = 0
    private var show = false
    var delayTime = 5L
    private val handler by lazy {
        val thread = HandlerThread("aaaa")
        thread.start()
        Handler(thread.looper)
    }

    fun showFloatMenu(context: Context) {
        if (!checkPermission(context))
            return
        if (show) {
            windowManager.removeView(directionView)
            windowManager.removeView(pointView)
            show = false
            return
        }
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusHeight = context.resources.getDimensionPixelSize(resourceId);
        }
        (context as Activity).finish()
        show = true
        windowManager = context.getSystemService(WindowManager::class.java)
        directionView = DirectionView(context)
        pointView = View(context)
        pointView.setBackgroundColor(Color.GREEN)
        directionView.layoutParams = SizeKit.matchParent
        directionView.setSureIconTouchEvent(moveTouchListener)
        directionView.locationChangeEvent = { x, y ->
            pointLayout.x += x.toInt()
            pointLayout.y += y.toInt()
            windowManager.updateViewLayout(pointView, pointLayout)
        }
        directionView.setDoneEvent({
            toast("点击了")
            saveClick(pointLayout.x.toFloat(), (pointLayout.y + statusHeight).toFloat())
        }, {
            createWorker(it.context)
            toast("完成")
            return@setDoneEvent true
        })
        windowManager.addView(directionView, menuLayout)

        pointView.layoutParams = SizeKit.matchParent
        windowManager.addView(pointView, pointLayout)
    }

    private fun checkPermission(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        if (!accessibilityManager.isEnabled) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return false
        }
        if (!Settings.canDrawOverlays(context)) {
            toast("该功能需要悬浮窗权限")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            context.startActivity(intent)
            return false
        }
        return true
    }

    private val _xyArrays = MutableLiveData<List<Pair<Float, Float>>>()
    val xyArrays: LiveData<List<Pair<Float, Float>>> = _xyArrays

    private val tempPair = LinkedList<Pair<Float, Float>>()

    private fun saveClick(x: Float, y: Float) {
        tempPair.add(x to y)
        _xyArrays.value = tempPair
    }

    private fun createWorker(context: Context) {
        if (tempPair.isEmpty())
            return
        val xArray = FloatArray(tempPair.size)
        val yArray = FloatArray(tempPair.size)
        for ((index, pair) in tempPair.withIndex()) {
            xArray[index] = pair.first
            yArray[index] = pair.second
        }
        tempPair.clear()
        val data = Data.Builder()
            .putFloatArray("xArray", xArray)
            .putFloatArray("yArray", yArray)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<AutoClickWorker>()
            .setInitialDelay(delayTime, TimeUnit.SECONDS)
            .setInputData(data)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
        workManager.enqueue(workRequest)
    }

    private fun calcTimeOffset(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute - Random.nextInt(6))
        calendar.set(Calendar.SECOND, Random.nextInt(60))
        return calendar.timeInMillis - System.currentTimeMillis()
    }

    private val moveTouchListener = object : View.OnTouchListener {
        private val lastPoint = PointF(0f, 0f)
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val x = event.rawX
            val y = event.rawY
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastPoint.set(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    val offsetX = x - lastPoint.x
                    val offsetY = y - lastPoint.y
                    menuLayout.x += offsetX.toInt()
                    menuLayout.y += offsetY.toInt()
                    lastPoint.set(x, y)
                    windowManager.updateViewLayout(v.parent as ViewGroup, menuLayout)
                }
            }
            return true
        }
    }
}