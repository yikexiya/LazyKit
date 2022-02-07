package com.yikexiya.lazykit.app

import android.app.Application
import android.graphics.PointF
import android.os.Process
import androidx.room.Room
import com.yikexiya.lazykit.ui.autoclick.Gesture
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class MainApplication : Application() , Thread.UncaughtExceptionHandler {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this, AppDatabase::class.java, "lazy-kit-db").build()
    }

    private val gestures = mutableListOf<Pair<Float, Float>>()
    fun addClickPoint(x: Float, y: Float): Boolean {
        if (gestures.size >= 10)
            return false
        gestures.add(x to y)
        return true
    }

    fun getAllGestures(): List<Pair<Float, Float>> = gestures
    fun clearGestures() {
        gestures.clear()
    }

    companion object {
        private var instance by Delegates.notNull<MainApplication>()
        fun instance() = instance
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        var needRecord = true
        val filesDir = File(filesDir, "error")
        if (!filesDir.exists()) {
            val success = filesDir.mkdirs()
            if (!success) {
                needRecord = false
            }
        }
        if (needRecord) {
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS", Locale.getDefault())
            val file = File(filesDir, simpleDateFormat.format(calendar.time))
            FileWriter(file).use {
                val printWriter = PrintWriter(it)
                e.printStackTrace(printWriter)
            }
        }
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}