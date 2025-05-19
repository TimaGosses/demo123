package com.example.demo123.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "Машина")
data class CarSupabaseRaw (
    @PrimaryKey val car_id: String,
    val Марка: String,
    val Цена_за_сутки: Int,
    val Описание: String,
    val Владелец: String,
    val Год_выпуска: Int,
    val VIN: String,
    val Модель: String,
    val Коробка_передач: Int,
    val Местоположение: Int,
    val Тип_кузова: Int,
    val imageUrls: List<String>, //путь к фото
    val updated_at: String,
    )

//конвертация для списка imageUrls
class Converts {
    @TypeConverter
    fun fromImageUrlsList(value: List<String>): String{
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toImageUrlsList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}