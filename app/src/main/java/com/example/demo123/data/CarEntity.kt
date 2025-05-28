package com.example.demo123.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.ColumnInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Данные в Room
@Entity(tableName = "car_table")
data class CarEntity (
    @PrimaryKey @ColumnInfo(name = "car_id") val car_id: String,
    @ColumnInfo(name = "Марка") val Марка: String,
    @ColumnInfo(name = "Цена_за_сутки") val Цена_за_сутки: Int,
    @ColumnInfo(name = "Описание") val Описание: String,
    @ColumnInfo(name = "Владелец") val Владелец: String,
    @ColumnInfo(name = "Год_выпуска") val Год_выпуска: Int,
    @ColumnInfo(name = "Модель") val Модель: String,
    @ColumnInfo(name = "Коробка_передач") val Коробка_передач: Int,
    @ColumnInfo(name = "Местоположение") val Местоположение: Int,
    @ColumnInfo(name = "Тип_кузова") val Тип_кузова: Int,
    @ColumnInfo(name = "Доступность") val Доступность: Boolean?,
    @ColumnInfo(name = "imageUrls") val imageUrls: List<String>, //List Url адресов фото
    @ColumnInfo(name = "updated_at") val updated_at: String,
    )

