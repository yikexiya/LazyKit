package com.yikexiya.lazykit.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.util.autoMeasure
import com.yikexiya.lazykit.util.dp
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
private val bottomDialogLp = WindowManager.LayoutParams(
    systemDisplayMetrics.widthPixels,
    (systemDisplayMetrics.heightPixels * 0.4f).toInt(),
    WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
    WindowManager.LayoutParams.FLAG_DIM_BEHIND,
    PixelFormat.TRANSLUCENT
).apply {
    gravity = Gravity.START or Gravity.BOTTOM
    dimAmount = 0.5f
    windowAnimations = R.style.BottomDialog
}
suspend fun confirmDialog(text: String): Boolean = suspendCoroutine {

}

fun View.showAsBottomDialog() = showAsDialog(bottomDialogLp)

fun View.showAsDialog(dialogLayoutParam: WindowManager.LayoutParams) {
    val windowManager = context.getSystemService(WindowManager::class.java)
    windowManager.addView(this, dialogLayoutParam)
}

fun View.dismissAsDialog() {
    val windowManager = context.getSystemService(WindowManager::class.java)
    windowManager.removeView(this)
}

//private val modelItemHeight = 44.dp.toInt()
//private val modelItemLp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, modelItemHeight)
//suspend fun <T> modelDialog(context: Context, modelItems: List<ModelItem>): T? = suspendCoroutine { continuation ->
//    val windowManager = context.getSystemService(WindowManager::class.java)
//    val layout = DialogViewGroup(context).apply {
//        setBackgroundColor(Color.WHITE)
//    }
//    modelItems.forEach {
//        it.attachTo(layout)
//    }
//    windowManager.addView(layout, dialogLayoutParams)
//}

//@SuppressLint("ViewConstructor")
//private class DialogViewGroup(context: Context) : ViewGroup(context) {
//
//    private val sure = button(this, "", {
//    }, SizeKit.ignoreLp)
//    private val cancel = button(this, "", {
//    }, SizeKit.ignoreLp)
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//    }
//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//    }
//}

//sealed class ModelItem(
//    val label: String,
//) {
//    abstract fun attachTo(parent: DialogViewGroup)
//    class TextItem(label: String): ModelItem(label) {
//        override fun attachTo(parent: ViewGroup) {
//            input(parent, label, modelItemLp)
//        }
//    }
//
//}