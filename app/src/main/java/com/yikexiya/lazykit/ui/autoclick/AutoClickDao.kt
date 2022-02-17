package com.yikexiya.lazykit.ui.autoclick

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoClickDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveGestureGroup(gestureGroup: GestureGroup): Long

    @Delete
    fun deleteGestureGroup(gestureGroup: GestureGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveGestures(gestures: List<Gesture>)

    @Transaction
    @Query("select * from GestureGroup where id in (select distinct(groupId) from Gesture)")
    fun getGestures(): Flow<List<GestureGroupRelation>>

    @Query("update GestureGroup set workRequestId = :workRequestId where id = :groupId")
    fun runGestureGroupWith(groupId: Long, workRequestId: String?)

    @Query("update GestureGroup set runTimeS = :secondInDay where id = :groupId")
    fun changeGestureGroupTime(groupId: Long, secondInDay: Long)
}