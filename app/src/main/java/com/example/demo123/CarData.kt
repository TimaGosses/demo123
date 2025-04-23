package com.example.demo123
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CarData(
    val car_id: String? = null,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    val Коробка_передач: Int,
    val Цена_за_сутки: Int,
    val Местоположение: Int,
    val VIN: String,
    val Владелец: String,
    val Описание: String
)
@Serializable
data class Transmission(
    val id: Int,
    val Название: String
)
@Serializable
data class City(
    val id: Int,
    val Название: String,
    val Регион: Int
)
@Serializable
data class Region(
    val id: Int,
    val Название: String
)
@Serializable
data class CarLocation(
    val id: Int? = null,
    val Город: Int
)
@Serializable
data class CarDataWithId(
    val car_id: String,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    val Коробка_передач: Int,
    val Цена_за_сутки: Int,
    val Местоположение: Int,
    val VIN: String,
    val Владелец: String
)

