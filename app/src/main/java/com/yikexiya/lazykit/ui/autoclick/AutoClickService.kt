package com.yikexiya.lazykit.ui.autoclick

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.HandlerThread
import android.view.accessibility.AccessibilityEvent
import com.yikexiya.lazykit.app.MainActivity
import com.yikexiya.lazykit.util.PermissionExt
import com.yikexiya.lazykit.util.log
import com.yikexiya.lazykit.util.toast

class AutoClickService : AccessibilityService() {

    private lateinit var handler: Handler

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
        val xArray = intent.getFloatArrayExtra("xArray") ?: return super.onStartCommand(intent, flags, startId)
        val yArray = intent.getFloatArrayExtra("yArray") ?: return super.onStartCommand(intent, flags, startId)
        PermissionExt.acquireScreenOn(this) {
            PermissionExt.unlockKeyGuard(MainActivity.instance) {
                dispatchAutoClick(xArray, yArray)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val clickCallback = object : AccessibilityService.GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            log("onCompleted")
            super.onCompleted(gestureDescription)
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            log("onCanceled")
            super.onCancelled(gestureDescription)
        }
    }
    private val runnable = object : Runnable {
        lateinit var xArrays: FloatArray
        lateinit var yArrays: FloatArray
        private var index = 0
        override fun run() {
            if (index >= xArrays.size) {
                index = 0
                return
            }
            val x = xArrays[index]
            val y = yArrays[index]
            path.reset()
            path.moveTo(x, y)
            toast("($x, $y)")
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 100L, 100L))
                .build()
            dispatchGesture(gesture, null, handler)
            handler.postDelayed(this, 5000)
            index++
        }
    }
    private val path = Path()
    private fun dispatchAutoClick(xArrays: FloatArray, yArrays: FloatArray) {
        runnable.xArrays = xArrays
        runnable.yArrays = yArrays
        handler.postDelayed(runnable, 2000)
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
}