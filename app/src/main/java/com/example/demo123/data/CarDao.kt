package com.example.demo123.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demo123.CarLists
import com.example.demo123.data.CarEntity
import kotlinx.coroutines.flow.Flow


//Интерфейс для работы с таблицей
@Dao
interface CarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCars(cars: List<CarEntity>)

    @Query("SELECT * FROM car_table")
    fun getAllCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM car_table WHERE LOWER(Марка) LIKE :query OR LOWER(Модель) LIKE :query")
    fun searсhCars(query: String): Flow<List<CarEntity>>

    @Query("SELECT updated_at FROM car_table ORDER BY updated_at DESC LIMIT 1")
    suspend fun getlastUpdateTime(): String?

    @Query("DELETE FROM car_table")
    suspend fun deleteallCars()

    @Query("SELECT * FROM car_table WHERE car_id = :carId")
    suspend fun getCarById(carId: String): CarEntity?

    @Query("SELECT * FROM car_table")
    suspend fun logCars(): List<CarEntity>

    @Query("SELECT * FROM car_table WHERE LOWER(Марка) LIKE '%' || :Марка || '%' AND LOWER(Модель) LIKE '%' || :Модель || '%'")
    suspend fun searchByBrandOrModel(Марка: String, Модель: String): List<CarLists>
}