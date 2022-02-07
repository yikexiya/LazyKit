package com.yikexiya.lazykit.ui.autoclick

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class AutoClickWorker(context: Context, workerParams: WorkerParameters) : Worker(context.applicationContext, workerParams) {
    override fun doWork(): Result {
        val groupId = inputData.getLong("groupId", 0)
        val xArray = inputData.getFloatArray("xArray") ?: return Result.failure()
        val yArray = inputData.getFloatArray("yArray") ?: return Result.failure()
        val durations = inputData.getLongArray("durations") ?: return Result.failure()
        val delayTimes = inputData.getLongArray("delayTimes") ?: return Result.failure()
        val accessibilityManager = applicationContext.getSystemService(AccessibilityManager::class.java)
        if (!accessibilityManager.isEnabled) return Result.failure()
        AutoClickService.performGestures(applicationContext, groupId, xArray, yArray, durations, delayTimes)
        return Result.success()
    }
}