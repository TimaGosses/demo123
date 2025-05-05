package com.example.demo123
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.util.UUID
import kotlinx.serialization.SerialName

// Модель для ответа с JOIN-запросами (сырые данные от Supabase)
@Serializable
data class CarResponseWithJoins(
    @SerialName("car_id") @Contextual val car_id: UUID,
    @SerialName("марка") val марка: String,
    @SerialName("модель") val модель: String,
    @SerialName("год_выпуска") val год_выпуска: Int,
    @SerialName("цена_за_сутки") val цена_за_сутки: Int,
    @SerialName("владелец") val владелец: String,
    @SerialName("коробка_передач") val коробка_передач_id: Int, // ID коробки передач
    @SerialName("местоположение") val местоположение_id: Int, // ID местоположения
    @SerialName("тип_кузова") val тип_кузова_id: Int, // ID типа кузова
    @SerialName("класс") val класс_id: Int, // ID класса автомобиля
    @SerialName("доступность") val доступность: Boolean,
    @SerialName("VIN") val vin: String,
    @SerialName("описание") val описание: String,
    @SerialName("Коробка_передач") val коробка_передач: TransmissionResponse?, // JOIN с таблицей Коробка_передач
    @SerialName("Местоположение") val местоположение: LocationResponse?, // JOIN с таблицей Местоположение
    @SerialName("Тип_кузова") val тип_кузова: BodyTypeResponse?, // JOIN с таблицей Тип_кузова
    @SerialName("Класс_автомобиля") val класс_автомобиля: CarClassResponse?, // JOIN с таблицей Класс_автомобиля
    @SerialName("Изображения_автомобилей") val изображения: List<ImageResponse> // JOIN с таблицей Изображения_автомобилей
)

// Модель для ответа из таблицы Машина (упрощённая, без JOIN)
@Serializable
data class CarResponse(
    @SerialName("car_id") @Contextual val car_id: UUID,
    @SerialName("марка") val марка: String,
    @SerialName("модель") val модель: String,
    @SerialName("год_выпуска") val год_выпуска: Int,
    @SerialName("цена_за_сутки") val цена_за_сутки: Int,
    @SerialName("владелец") val владелец: String,
    @SerialName("коробка_передач") val коробка_передач: Int,
    @SerialName("местоположение") val местоположение: Int,
    @SerialName("тип_кузова") val тип_кузова: Int,
    @SerialName("класс") val класс: Int,
    @SerialName("доступность") val доступность: Boolean,
    @SerialName("VIN") val vin: String,
    @SerialName("описание") val описание: String,
    @SerialName("Изображения_автомобилей") val изображения: List<ImageResponse>
)

// Модель для ответа из таблицы Коробка_передач
@Serializable
data class TransmissionResponse(
    @SerialName("id") val id: Int,
    @SerialName("название") val название: String
)

// Модель для ответа из таблицы Местоположение
@Serializable
data class LocationResponse(
    @SerialName("id") val id: Int,
    @SerialName("город") val город: String,
    @SerialName("регион") val регион: String
)

// Модель для ответа из таблицы Тип_кузова
@Serializable
data class BodyTypeResponse(
    @SerialName("id") val id: Int,
    @SerialName("название") val название: String
)

// Модель для ответа из таблицы Класс_автомобиля
@Serializable
data class CarClassResponse(
    @SerialName("id") val id: Int,
    @SerialName("название") val название: String
)

// Модель для ответа из таблицы Изображения_автомобилей
@Serializable
data class ImageResponse(
    @SerialName("car_id") @Contextual val car_id: UUID,
    @SerialName("user_id") @Contextual val user_id: UUID,
    @SerialName("image_id") @Contextual val image_id: UUID,
    @SerialName("url_изображения") val url_изображения: String
)

// Модель Car для использования в приложении (упрощённая и преобразованная)
data class Car(
    val car_id: UUID,
    val Марка: String,
    val Модель: String,
    val Год_выпуска: Int,
    val Цена_за_сутки: Int,
    val Владелец: String,
    val Коробка_передач: String, // Название коробки передач (не ID)
    val Город: String, // Из таблицы Местоположение
    val Регион: String, // Из таблицы Местоположение
    val Тип_кузова: String, // Название типа кузова (не ID)
    val Класс: String, // Название класса (не ID)
    val Доступность: Boolean,
    val VIN: String,
    val Описание: String,
    val imageUrls: List<String> // Список URL-адресов изображений
)

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
data class BodyType(
    val id: Int,
    val Название: String
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
data class CarImageS(
    val carId: String,
    val imageUrl: String
)