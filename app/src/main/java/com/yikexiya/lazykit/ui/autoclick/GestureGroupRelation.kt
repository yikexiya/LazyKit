package com.yikexiya.lazykit.ui.autoclick

import androidx.room.Embedded
import androidx.room.Relation

data class GestureGroupRelation(
    @Embedded
    val gestureGroup: GestureGroup,

    @Relation(parentColumn = "id", entityColumn = "groupId")
    val gestures: List<Gesture> = emptyList()
) {
    override fun toString(): String {
        return "$gestureGroup, $gestures"
    }
}
