package com.example.demo123.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.example.demo123.UserData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

//Данные в Room
@Entity(tableName = "car_table")
@TypeConverters(Converters::class) //конвертер для UserData
@Serializable
data class CarEntity(
    @PrimaryKey @ColumnInfo(name = "car_id") val car_id: String,
    @ColumnInfo(name = "Марка") val Марка: String,
    @ColumnInfo(name = "Цена_за_сутки") val Цена_за_сутки: Int,
    @ColumnInfo(name = "Описание") val Описание: String?,
    @ColumnInfo(name = "Владелец") val Владелец: String,
    @ColumnInfo(name = "Год_выпуска") val Год_выпуска: Int,
    @ColumnInfo(name = "Модель") val Модель: String,
    @ColumnInfo(name = "Коробка_передач") val Коробка_передач: Int,
    @ColumnInfo(name = "Название_коробки_передач") val Название_коробки_передач: String,
    @ColumnInfo(name = "Местоположение") val Местоположение: Int,
    //@ColumnInfo(name = "Название_региона") val Название_региона: String,
    @ColumnInfo(name = "Название_города") val Название_города: String,
    @ColumnInfo(name = "Тип_кузова") val Тип_кузова: Int,
    @ColumnInfo(name = "Название_типа_кузова") val Название_типа_кузова: String,
    @ColumnInfo(name = "Доступность") val Доступность: Boolean?,
    @ColumnInfo(name = "imageUrls") val imageUrls: List<String>,
    @ColumnInfo(name = "updated_at") val updated_at: String
)

class Converters {

    @TypeConverter
    fun fromList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromUserData(userData: UserData?): String? {
        return userData?.let { Json.encodeToString(it) }
    }
    @TypeConverter
    fun toUserData(userDatString: String?): UserData? {
        return userDatString?.let { Json.decodeFromString<UserData>(it) }
    }
}

