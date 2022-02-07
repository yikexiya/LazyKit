package com.yikexiya.lazykit.ui.autoclick

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["groupId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = GestureGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class Gesture(
    val x: Float,
    val y: Float,
    /**
     * 手势的延迟时间，单位秒
     */
    val delayTimeS: Long,
    /**
     * 手势的持续时间，单位毫秒
     */
    val durationTimeMs: Long,
    /**
     * 所属的分组，当组id是0的时候表示不属于任何分组，此时只是内存数据
     */
    val groupId: Long,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Gesture

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "delayTime=${delayTimeS}s, duration=${durationTimeMs}ms, ($x, $y)"
    }
}