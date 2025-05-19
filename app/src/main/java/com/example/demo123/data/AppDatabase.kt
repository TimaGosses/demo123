package com.example.demo123.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CarEntity::class], version = 1, exportSchema = false)
 abstract class AppDatabase : RoomDatabase() {
    abstract fun carDao() : CarDao
 }