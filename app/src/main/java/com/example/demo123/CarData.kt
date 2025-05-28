package com.example.demo123
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.util.UUID
import kotlinx.serialization.SerialName
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@Serializable
data class CarData( //трогать нельзя
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
data class BodyType(
    val id: Int,
    val Название: String
)
@Serializable
data class Region(
    val id: Int,
    val Название: String
)
@Serializable
data class Brand(
    val id: Int,
    val Марка: String
)
@Serializable
data class Price(
    val id: Int,
    val Цена_за_сутки: String
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
@Serializable
data class CarResponse(
    val carId: String,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    val Цена_за_сутки: Int,
    val Коробка_передач: Int,
    val Местоположение: Int,
    val Владелец: String
)

@Serializable
data class Car(
    val car_id: String,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    val Коробка_передач: Int,
    val Цена_за_сутки: Int,
    val Местоположение: Int,
    val imageUrls: List<String>,
    val Владелец: String
)
@Serializable
data class CarLists(
    val car_id: String,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    val Коробка_передач: Int,
    val Цена_за_сутки: Int,
    val Местоположение: Int,
    val imageUrls: List<String>,
    val Владелец: String,
    val Описание: String,
    val Доступность: Boolean?,
    val updated_at: String,
    val Тип_кузова: Int
)
@Serializable
data class CarRaw(
    val car_id: String,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    val Коробка_передач: Int,
    val Цена_за_сутки: Int,
    val Местоположение: Int,
    val image_url: String,
    val Владелец: String,
    val Тип_кузова: Int,
    val Доступность: Boolean?,
    val Описание: String? = null
)
@Serializable
data class EmbeddedCarImage( // Назвал его EmbeddedCarImage, чтобы отличать от вашего CarImageS
    val image_url: String
)
@Serializable
data class CarSupabaseRaw(
    @SerialName("car_id") val car_id: String,
    @SerialName("Марка") val Марка: String,
    @SerialName("Модель") val Модель: String,
    @SerialName("Год_выпуска") val Год_выпуска: Int,
    @SerialName("Коробка_передач") val Коробка_передач: Int,
    @SerialName("Цена_за_сутки") val Цена_за_сутки: Int,
    @SerialName("Местоположение") val Местоположение: Int,
    @SerialName("Владелец") val Владелец: String,
    @SerialName("Тип_кузова") val Тип_кузова: Int,
    @SerialName("Доступность") val Доступность: Boolean?,
    @SerialName("Описание") val Описание: String,
    @SerialName("updated_at") val updated_at: String,
    //val parsedDataTime = LocalDateTime.parse(updated_at, DateTimeFormatter.ISO_DATE_TIME)


    // ЭТОТ ПОЛЕ СООТВЕТСТВУЕТ ВЛОЖЕННЫМ ДАННЫМ ОТ select(..., Изображение_автомобиля(...))
    // @SerialName ДОЛЖЕН СОВПАДАТЬ С ИМЕНЕМ СВЯЗИ В БАЗЕ ("Изображение_автомобиля")
    // Тип должен быть List<Класс_для_вложенного_объекта> (то есть List<EmbeddedCarImage>)
    @SerialName("Изображение_автомобиля") val images: List<String> = emptyList()
)
