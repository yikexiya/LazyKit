package com.yikexiya.lazykit.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yikexiya.lazykit.ui.autoclick.AutoClickDao
import com.yikexiya.lazykit.ui.autoclick.Gesture
import com.yikexiya.lazykit.ui.autoclick.GestureGroup

@Database(entities = [Gesture::class, GestureGroup::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun autoClickDao(): AutoClickDao
}