package com.example.demo123.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase


//создание экземпляра базы данных
object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        Log.d("AppDatabase", "База данных создана")
                    }
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        Log.d("AppDatabase", "База данных открыта")
                    }
                })
                .build().also { INSTANCE = it }
        }
    }

    fun provideCarDao(database: AppDatabase): CarDao {
        return database.carDao()
    }
}