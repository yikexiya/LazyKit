package com.yikexiya.lazykit.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.yikexiya.lazykit.app.MainApplication
import java.io.File

private const val TAG = "WSW"

fun log(message: String) {
    Log.d(TAG, message)
}

fun logW(message: String) {
    Log.w(TAG, message)
}

fun toast(message: String) {
    Toast.makeText(MainApplication.instance(), message, Toast.LENGTH_LONG).show()
}

fun getImageDir(context: Context): File {
    val filesDir = context.filesDir
    val file = File(filesDir, "images")
    if (!file.exists() && !file.mkdirs())
        throw RuntimeException("create image dir failed!")
    return file
}