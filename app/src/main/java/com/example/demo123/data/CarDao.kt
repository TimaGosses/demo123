package com.example.demo123.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM Машина")
    fun getAllCars(): Flow<List<CarEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertCars(cars: List<CarEntity>)

    @Query("DELETE FROM Машина")
    suspend fun deleteallCars()

    @Query("SELECT MAX(created_at) FROM Машина")
    suspend fun getlastUpdateTime(): String?
}