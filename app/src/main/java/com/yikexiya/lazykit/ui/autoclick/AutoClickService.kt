package com.yikexiya.lazykit.ui.autoclick

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Path
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.view.accessibility.AccessibilityEvent
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.app.MainActivity
import com.yikexiya.lazykit.app.MainApplication
import com.yikexiya.lazykit.util.PermissionExt
import com.yikexiya.lazykit.util.log
import com.yikexiya.lazykit.util.logW

class AutoClickService : AccessibilityService() {

    private lateinit var handler: Handler
    private var fingerView: FingerView? = null

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("AutoClick").also { it.start() }
        handler = Handler(thread.looper)
    }

    override fun onDestroy() {
        handler.looper.quitSafely()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (val action = intent.getStringExtra(SERVICE_ACTION)) {
            ACTION_CLICK -> {
                val x = intent.getFloatExtra("x", 0f)
                val y = intent.getFloatExtra("y", 0f)
                dispatchClick(x, y)
            }
            ACTION_TOGGLE_VIEW -> {
                val previewFingerView = fingerView
                if (previewFingerView == null) {
                    FingerView(this).run {
                        fingerView = this
                        show()
                    }
                } else {
                    previewFingerView.remove()
                    fingerView = null
                }
            }
            ACTION_PERFORM_GESTURES -> {
                runnable.groupId = intent.getLongExtra("groupId", 0)
                val xArray = intent.getFloatArrayExtra("xArray") ?: return super.onStartCommand(intent, flags, startId)
                val yArray = intent.getFloatArrayExtra("yArray") ?: return super.onStartCommand(intent, flags, startId)
                val durations = intent.getLongArrayExtra("durations") ?: return super.onStartCommand(intent, flags, startId)
                val delayTimes = intent.getLongArrayExtra("delayTimes") ?: return super.onStartCommand(intent, flags, startId)
                PermissionExt.acquireScreenOn(this) {
                    PermissionExt.unlockKeyGuard(MainActivity.instance) {
                        dispatchAutoClick(xArray, yArray, durations, delayTimes)
                    }
                }
            }
            else -> logW("can't find action code: $action")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val autoClickCallback: GestureResultCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            handler.post(runnable)
            log("click")
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            log("onCanceled")
        }
    }
    private val runnable = object : Runnable {
        lateinit var xArrays: FloatArray
        lateinit var yArrays: FloatArray
        lateinit var durations: LongArray
        lateinit var delayTimes: LongArray
        var groupId = 0L
        private var index = 0
        override fun run() {
            log("perform $index")
            if (index >= xArrays.size) {
                index = 0
                MainApplication.instance().database.autoClickDao().changeGestureGroupRunningTo(groupId, false)
                return
            }
            val x = xArrays[index]
            val y = yArrays[index]
            val duration = durations[index]
            val delayTime = delayTimes[index]
            path.reset()
            path.moveTo(x, y)
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, delayTime * 1000, duration))
                .build()
            dispatchGesture(gesture, autoClickCallback, handler)
            index++
        }
    }

    private val path = Path()
    private fun dispatchAutoClick(xArrays: FloatArray, yArrays: FloatArray, durations: LongArray, delayTimes: LongArray) {
        runnable.xArrays = xArrays
        runnable.yArrays = yArrays
        runnable.durations = durations
        runnable.delayTimes = delayTimes
        handler.post(runnable)
    }

    private val clickPath = Path()
    private val clickCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            fingerView?.show()
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            logW("click is canceled")
        }
    }
    private fun dispatchClick(x: Float, y: Float) {
        clickPath.reset()
        clickPath.moveTo(x, y)
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(clickPath, 100L, 100L))
            .build()
        dispatchGesture(gesture, clickCallback, handler)
    }

    override fun onServiceConnected() {
        log("onServiceConnected: ")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log("onUnbind: ")
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        log("onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        log("onInterrupt: ")
    }

    companion object {
        private const val SERVICE_ACTION = "SERVICE_ACTION"
        private const val ACTION_CLICK = "ACTION_CLICK"
        private const val ACTION_TOGGLE_VIEW = "ACTION_TOGGLE_VIEW"
        private const val ACTION_PERFORM_GESTURES = "ACTION_PERFORM_GESTURES"
        private const val ACTION_SAVE = "ACTION_SAVE"
        fun click(context: Context, x: Float, y: Float) {
            val intent = Intent(context, AutoClickService::class.java)
            intent.putExtra(SERVICE_ACTION, ACTION_CLICK)
            intent.putExtra("x", x)
            intent.putExtra("y", y)
            context.startService(intent)
        }

        fun showView(context: Context) {
            val intent = Intent(context, AutoClickService::class.java)
            intent.putExtra(SERVICE_ACTION, ACTION_TOGGLE_VIEW)
            context.startService(intent)
        }

        fun performGestures(context: Context, groupId: Long, xArray: FloatArray, yArray: FloatArray, durations: LongArray, delayTimes: LongArray) {
            val intent = Intent(context, AutoClickService::class.java)
            intent.putExtra(SERVICE_ACTION, ACTION_PERFORM_GESTURES)
            intent.putExtra("groupId", groupId)
            intent.putExtra("xArray", xArray)
            intent.putExtra("yArray", yArray)
            intent.putExtra("durations", durations)
            intent.putExtra("delayTimes", delayTimes)
            context.startService(intent)
        }

    }
}