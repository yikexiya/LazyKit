package com.yikexiya.lazykit.ui.autoclick

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class GestureGroup(
    val name: String,
    /**
     * 已0点为基准，24小时制，运行的时间，单位秒
     */
    val runTimeS: Long,
    val isRunning: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GestureGroup

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "group: id=$id, name=$name, isRunning=$isRunning runAt=${runTimeS}s"
    }
}