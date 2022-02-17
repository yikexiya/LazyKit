package com.yikexiya.lazykit.util

import android.content.Context
import java.io.File

object FileExt {
    fun getMusicDir(context: Context) = getFileDir(context, "music")

    fun getImageDir(context: Context) = getFileDir(context, "image")

    fun getVideoDir(context: Context) = getFileDir(context, "video")

    private fun getFileDir(context: Context, type: String): File {
        val filesDir = context.filesDir
        val file = File(filesDir, type)
        if (!file.exists() && !file.mkdirs())
            throw RuntimeException("create $type dir failed!")
        return file
    }
}