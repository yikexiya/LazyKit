package com.yikexiya.lazykit.util

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.yikexiya.lazykit.app.MainApplication

object PermissionExt {
    const val PERMISSION_FLOAT_WINDOW = 0
    const val PERMISSION_ACCESSIBILITY = 1

    fun hasPermission(context: Context, permission: Int): Boolean {
        return when (permission) {
            PERMISSION_FLOAT_WINDOW -> Settings.canDrawOverlays(context)
            PERMISSION_ACCESSIBILITY -> context.getSystemService(AccessibilityManager::class.java).isEnabled
            else -> false
        }
    }

    fun requestPermission(context: Context, permission: Int) {
        when (permission) {
            PERMISSION_FLOAT_WINDOW -> {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            PERMISSION_ACCESSIBILITY -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    inline fun acquireScreenOn(context: Context, afterScreenOn: () -> Unit) {
        val powerManager = context.getSystemService(PowerManager::class.java)
        if (!powerManager.isInteractive) {
            val wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, MainApplication::javaClass.name)
            wakeLock.acquire(10*60*1000L /*10 minutes*/)
            wakeLock.release()
        }
        afterScreenOn()
    }

    inline fun unlockKeyGuard(activity: Activity, crossinline onUnlocked: () -> Unit) {
        val keyguardManager = activity.getSystemService(KeyguardManager::class.java)
        if (keyguardManager.isKeyguardLocked) {
            if (!keyguardManager.isKeyguardSecure) {
                keyguardManager.requestDismissKeyguard(activity, object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        onUnlocked()
                    }

                    override fun onDismissError() {
                        log("dismiss guard error")
                    }
                })
            }
        } else {
            onUnlocked()
        }
    }
}