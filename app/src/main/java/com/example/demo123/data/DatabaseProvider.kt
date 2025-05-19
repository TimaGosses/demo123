package com.example.demo123.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase{
        return INSTANCE ?: synchronized(this@DatabaseProvider) {
            val intance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
            INSTANCE = intance
            intance
        }
    }
}