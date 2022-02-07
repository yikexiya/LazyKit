package com.yikexiya.lazykit.ui.autoclick

import android.graphics.PointF
import androidx.room.TypeConverter

class PointsConverter {
    @TypeConverter
    fun toPointsFrom(string: String): List<PointF> {
        if (string.isEmpty())
            return emptyList()
        val (xString, yString) = string.split(':')
        val xs = xString.split(',')
        val ys = yString.split(',')
        val list = mutableListOf<PointF>()
        for (i in xs.indices) {
            list.add(PointF(xs[i].toFloat(), ys[i].toFloat()))
        }
        return list
    }

    @TypeConverter
    fun toStringFrom(points: List<PointF>): String {
        if (points.isEmpty())
            return ""
        val x = StringBuilder()
        val y = StringBuilder()
        points.forEach {
            x.append(it.x).append(',')
            y.append(it.y).append(',')
        }
        x.deleteAt(x.lastIndex)
        y.deleteAt(y.lastIndex)
        return "$x:$y"
    }
}