package com.example.demo123.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

//данные для UI
@Serializable
data class CarList( //трогать нельзя
    val car_id: String,
    val Марка: String,
    val Цена_за_сутки: Int,
    val Описание: String,
    val Владелец: String,
    val Год_выпуска: Int,
    val Модель: String,
    val Коробка_передач: Int,
    val Местоположение: Int,
    val Тип_кузова: Int,
    val imageUrls: List<String>, //путь к фото
    @Contextual val updated_at: LocalDateTime
)


