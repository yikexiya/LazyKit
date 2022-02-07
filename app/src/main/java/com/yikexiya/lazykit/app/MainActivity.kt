package com.yikexiya.lazykit.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.theme.ThemeKit
import com.yikexiya.lazykit.ui.autoclick.ScreenCaptureService
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var screenshotLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setShowWhenLocked(true)
        setTheme(ThemeKit.getTheme())
        setContentView(R.layout.activity_main)
        screenshotLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@registerForActivityResult
            ScreenCaptureService.capture(this, result.resultCode, data)
        }
    }

    companion object {
        var instance by Delegates.notNull<MainActivity>()
        private set
    }
}