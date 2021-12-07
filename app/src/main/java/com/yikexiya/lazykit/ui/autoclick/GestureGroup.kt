package com.yikexiya.lazykit.ui.autoclick

import java.util.UUID

class GestureGroup(
    val name: String
) {
    private val id = UUID.randomUUID().toString()
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

}