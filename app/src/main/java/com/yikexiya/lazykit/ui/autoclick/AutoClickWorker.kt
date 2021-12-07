package com.yikexiya.lazykit.ui.autoclick

import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class AutoClickWorker(context: Context, workerParams: WorkerParameters) : Worker(context.applicationContext, workerParams) {
    override fun doWork(): Result {
        val xArray = inputData.getFloatArray("xArray") ?: return Result.failure()
        val yArray = inputData.getFloatArray("yArray") ?: return Result.failure()
        val accessibilityManager = applicationContext.getSystemService(AccessibilityManager::class.java)
        if (!accessibilityManager.isEnabled) return Result.failure()
        val intent = Intent(applicationContext, AutoClickService::class.java)
        intent.putExtra("xArray", xArray)
        intent.putExtra("yArray", yArray)
        applicationContext.startService(intent)
        return Result.success()
    }
}