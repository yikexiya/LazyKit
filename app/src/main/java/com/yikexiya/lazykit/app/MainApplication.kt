package com.yikexiya.lazykit.app

import android.app.Application
import androidx.room.Room
import kotlin.properties.Delegates

class MainApplication : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this, AppDatabase::class.java, "lazy-kit-db").build()
    }

    companion object {
        private var instance by Delegates.notNull<MainApplication>()
        fun instance() = instance
    }
}