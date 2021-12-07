package com.yikexiya.lazykit.ui.autoclick

import android.app.Application
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.yikexiya.lazykit.util.PermissionExt
import com.yikexiya.lazykit.util.log
import com.yikexiya.lazykit.util.toast

class AttendViewModel(application: Application) : AndroidViewModel(application) {
    private val workQuery = WorkQuery.Builder.fromTags(listOf(AutoClickWorker::class.java.name))
        .addStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED, WorkInfo.State.CANCELLED))
        .build()
    val works = WorkManager.getInstance(application).getWorkInfosLiveData(workQuery)
    private val _gestureGroups = MutableLiveData<List<GestureGroup>>()
    val gestureGroups: LiveData<List<GestureGroup>> = _gestureGroups

    fun newGestureGroup() {
        val context = getApplication<Application>()
        if (!PermissionExt.hasPermission(context, PermissionExt.PERMISSION_FLOAT_WINDOW)) {
            toast("需要悬浮窗权限")
            PermissionExt.requestPermission(context, PermissionExt.PERMISSION_FLOAT_WINDOW)
            return
        }
//        if (!PermissionExt.hasPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)) {
//            toast("需要无障碍权限")
//            PermissionExt.requestPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)
//            return
//        }
//        AttendManager.showFloatMenu(context)
        val windowManager = context.getSystemService(WindowManager::class.java)
        val lp = WindowManager.LayoutParams(
            400,
            400,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            PixelFormat.OPAQUE
        )
//        lp.gravity = Gravity.START or Gravity.TOP
        val view = object : View(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                log("mode=${MeasureSpec.getMode(widthMeasureSpec)}, ${MeasureSpec.EXACTLY} width=${MeasureSpec.getSize(widthMeasureSpec)}")
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }.apply {
            setBackgroundColor(Color.RED)
        }
        windowManager.addView(view, lp)
    }
    fun deleteGestureGroup(gestureGroup: GestureGroup) {}
    fun editGestureGroup(gestureGroup: GestureGroup) {
    }
    fun showGestureGroup(gestureGroup: GestureGroup) {}
    fun playGestureGroup(gestureGroup: GestureGroup) {}
}