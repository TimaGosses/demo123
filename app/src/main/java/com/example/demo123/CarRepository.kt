package com.example.demo123


import android.util.Log
import androidx.compose.foundation.layout.Column
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName




// Репозиторий для работы с данными об автомобилях
class CarRepository(private val app: MyApplication) {

    // Асинхронная функция для получения списка автомобилей из Supabase
    suspend fun getCars(): List<Car> {
        Log.d("CarRepository", "Starting query to Supabase...")

        // Выполняем запрос к таблице Машина с JOIN для связанных таблиц
        val response = app.supabase.from("Машина")
            .select(columns = Columns.raw("""
                car_id, марка, модель, год_выпуска, цена_за_сутки, владелец, коробка_передач, местоположение, тип_кузова, класс, доступность, VIN, описание,
                Коробка_передач!left(название),
                Местоположение!left(город, регион),
                Тип_кузова!left(название),
                Класс_автомобиля!left(название),
                Изображения_автомобилей!left(car_id, user_id, image_id, url_изображения)
            """.trimIndent()))
            .decodeList<CarResponseWithJoins>() // Используем новую модель для JOIN

        Log.d("CarRepository", "Received ${response.size} cars from Supabase: $response")

        // Преобразуем сырой ответ в список объектов Car
        return response.map { carResponse ->
            Car(
                car_id = carResponse.car_id,
                Марка = carResponse.марка,
                Модель = carResponse.модель,
                Год_выпуска = carResponse.год_выпуска,
                Цена_за_сутки = carResponse.цена_за_сутки,
                Владелец = carResponse.владелец,
                Коробка_передач = carResponse.коробка_передач?.название ?: "Не указано",
                Город = carResponse.местоположение?.город ?: "Не указано",
                Регион = carResponse.местоположение?.регион ?: "Не указано",
                Тип_кузова = carResponse.тип_кузова?.название ?: "Не указано",
                Класс = carResponse.класс_автомобиля?.название ?: "Не указано",
                Доступность = carResponse.доступность,
                VIN = carResponse.vin,
                Описание = carResponse.описание,
                imageUrls = carResponse.изображения.map { image -> image.url_изображения } // Исправляем доступ к полю
            )
        }
    }
}