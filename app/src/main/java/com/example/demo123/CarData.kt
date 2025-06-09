package com.example.demo123
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
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
    val Описание: String,
    val Тип_кузова: Int
)
@Serializable
data class Transmission(
    val id: Int,
    val Название_коробки_передач: String
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
    val Название_типа_кузова: String
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
data class CarListes(
    val car_id: String,
    val Марка: String,
    val Цена_за_сутки: Int,
    val Описание: String?,
    val Владелец: String,
    val Год_выпуска: Int,
    val Модель: String,
    val Коробка_передач: Int,
    val Название_коробки_передач: String,
    val Местоположение: Int,
   // val Название_региона: String,
    val Название_города: String,
    val Тип_кузова: Int,
    val Название_типа_кузова: String,
    val Доступность: Boolean?,
    val imageUrls: List<String>,
    val updated_at: String
) : JavaSerializable

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
    @SerialName("Владелец") val Владелец: String,
    val Описание: String,
    val Доступность: Boolean?,
    val updated_at: String,
    val Тип_кузова: Int,
): java.io.Serializable
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
    @SerialName("Описание") val Описание: String?,
    @SerialName("updated_at") val updated_at: String,
    //val parsedDataTime = LocalDateTime.parse(updated_at, DateTimeFormatter.ISO_DATE_TIME)


    // ЭТОТ ПОЛЕ СООТВЕТСТВУЕТ ВЛОЖЕННЫМ ДАННЫМ ОТ select(..., Изображение_автомобиля(...))
    // @SerialName ДОЛЖЕН СОВПАДАТЬ С ИМЕНЕМ СВЯЗИ В БАЗЕ ("Изображение_автомобиля")
    // Тип должен быть List<Класс_для_вложенного_объекта> (то есть List<EmbeddedCarImage>)
    @SerialName("Изображение_автомобиля") val images: List<EmbeddedCarImage> = emptyList(),
    //@SerialName("Номер_телефона") val Номер_телефона: String?
)

@Serializable
data class CarSupabaseRaws(
    val car_id: String,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    @SerialName("Коробка_передач_автомобиля") val Коробка_передач: Transmissions,
    val Цена_за_сутки: Int,
    @SerialName("Город") val Город: Citys? = null, // Местоположение связано с Город
    val Владелец: String,
    val Описание: String? = null,
    val Доступность: Boolean? = true,
    val updated_at: String,
    @SerialName("Местонахождение_автомобиля") val Местонахождение_автомобиля: Location? = null,
    @SerialName("Тип_кузова") val Тип_кузова: BodyTypes,
    @SerialName("Изображение_автомобиля") val images: List<EmbeddedCarImage> = emptyList(),
    //@SerialName("Регион") val Регион: Regions? = null
)

@Serializable
data class Transmissions(
    val id: Int,
    @SerialName("Название_коробки_передач") val Название_коробки_передач: String
)

@Serializable
data class Citys(
    val id: Int,
    @SerialName("Название_города") val Название_города: String, // Используем Название_города для соответствия CarEntity
    //@SerialName("Регион") val Регион: Regions // Используем Название_города для соответствия CarEntity
)

@Serializable
data class Location(
    val id: Int,
    @SerialName("Город") val Город: Citys? = null


)

@Serializable
data class BodyTypes(
    val id: Int,
    @SerialName("Название_типа_кузова") val Название_типа_кузова: String
)

@Serializable
data class Regions(
    val id: Int,
    @SerialName("Название") val Название_региона: String
)

