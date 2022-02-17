package com.yikexiya.lazykit.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.yikexiya.lazykit.app.MainApplication
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "WSW"

fun log(message: String) {
    Log.d(TAG, message)
}

fun logW(message: String) {
    Log.w(TAG, message)
}

fun logInFile(message: String) {
    val filesDir = MainApplication.instance().filesDir
    val dir = File(filesDir, "log")
    if (!dir.exists() && !dir.mkdirs())
        throw RuntimeException("create log dir failed")
    val errorFile = File(dir, "errorInfo")
    FileWriter(errorFile, true).use {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timeString = simpleDateFormat.format(calendar.time)
        it.write("${timeString}: ${message}\n")
    }
}

fun toast(message: String) {
    Toast.makeText(MainApplication.instance(), message, Toast.LENGTH_LONG).show()
}

fun getImageDir(context: Context): File {
    val filesDir = context.filesDir
    val file = File(filesDir, "image")
    if (!file.exists() && !file.mkdirs())
        throw RuntimeException("create image dir failed!")
    return file
}