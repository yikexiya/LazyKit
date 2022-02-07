package com.yikexiya.lazykit.ui.autoclick

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.view.children
import com.yikexiya.lazykit.app.MainApplication
import com.yikexiya.lazykit.theme.button
import com.yikexiya.lazykit.theme.input
import com.yikexiya.lazykit.util.*

class GestureDialog(context: Context) : ViewGroup(context) {
    private val windowManager by lazy { context.getSystemService(WindowManager::class.java) }
    private val groupName = input(this, "手势名称", modelItemLp).apply {
        isSingleLine = true
        imeOptions = EditorInfo.IME_ACTION_NEXT
    }

    private val runningTime = input(this, "时分秒，用英文冒号分割，24小时制", modelItemLp).apply {
        isSingleLine = true
        inputType = EditorInfo.TYPE_CLASS_TEXT
        imeOptions = EditorInfo.IME_ACTION_NEXT
    }

    private val gesturesDelayTime = input(this, "第一个点之后的延迟m秒", modelItemLp).apply {
        isSingleLine = true
        inputType = EditorInfo.TYPE_CLASS_NUMBER
        imeOptions = EditorInfo.IME_ACTION_NEXT
    }

    private val sure = button(this, "确定", {
        val time: Long? = try {
            val s = runningTime.text?.toString() ?: ""
            val split = s.split(':')
            val second = split[0].toInt() *  3600 + split[1].toInt() * 60 + split[0].toInt()
            second.toLong()
        } catch (e: Exception) {
            null
        }
        if (time == null) {
            toast("时间解析错误，格式是 HH:mm:ss")
            return@button
        }
        val delayTime = gesturesDelayTime.text?.toString()?.toLongOrNull()
        if (delayTime == null) {
            toast("延迟解析错误，输入整数秒")
            return@button
        }
        val name = groupName.text?.toString() ?: ""
        val allGestures = MainApplication.instance().getAllGestures()
        val delayTimes = allGestures.mapIndexed { index, _ ->
            if (index == 0) delayTime else 10
        }
        windowManager.removeView(this)
        sureEvent?.invoke(name, time, delayTimes, allGestures)
    })

    private val cancel = button(this, "取消", {
        windowManager.removeView(this)
    })

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun shouldDelayChildPressedState() = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        children.forEach { it.autoMeasure(this) }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        groupName.layout(0, 0)
        runningTime.layout(0, groupName.bottom)
        gesturesDelayTime.layout(0, runningTime.bottom)
        constraintChildrenWith(measuredHeight - sure.measuredHeight, listOf(sure, cancel))
    }

    var sureEvent: ((groupName: String, runningTimeS: Long, delayTimeS: List<Long>, gesturePoints: List<Pair<Float, Float>>) -> Unit)? = null

    fun show() {
        windowManager.addView(this, dialogLayoutParams)
    }

    companion object {
        private val systemDisplayMetrics = Resources.getSystem().displayMetrics
        private val dialogLayoutParams = WindowManager.LayoutParams(
            (systemDisplayMetrics.widthPixels * 0.8f).toInt(),
            (systemDisplayMetrics.heightPixels * 0.6f).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            dimAmount = 0.5f
            windowAnimations = android.R.style.Animation_Dialog
        }
        private val modelItemHeight = 44.dp.toInt()
        private val modelItemLp = LayoutParams(LayoutParams.MATCH_PARENT, modelItemHeight)
    }
}