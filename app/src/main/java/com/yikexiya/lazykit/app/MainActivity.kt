package com.yikexiya.lazykit.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.theme.ThemeKit
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setShowWhenLocked(true)
        setTheme(ThemeKit.getTheme())
        setContentView(R.layout.activity_main)
    }

    companion object {
        var instance by Delegates.notNull<MainActivity>()
        private set
    }
}