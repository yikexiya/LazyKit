package com.yikexiya.lazykit.util

import android.util.Log
import android.widget.Toast
import com.yikexiya.lazykit.app.MainApplication

private const val TAG = "WSW"

fun log(message: String) {
    Log.d(TAG, message)
}

fun toast(message: String) {
    Toast.makeText(MainApplication.instance(), message, Toast.LENGTH_LONG).show()
}