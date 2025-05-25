package com.example.demo123.data

import android.util.Log
import com.example.demo123.CarLists
import com.example.demo123.CarSupabaseRaw
import com.example.demo123.EmbeddedCarImage
import com.example.demo123.MyApplication
import com.example.demo123.data.CarEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlin.collections.mapNotNull
import kotlin.time.Duration.Companion.hours

class CarRepository(
    private val app: MyApplication
) {
    private val supabaseClient: SupabaseClient = app.supabase
    private val dao = DatabaseProvider.getDatabase(app).carDao()

    init {
        Log.d("CarRepository", "Инициализация Supabase: ${supabaseClient != null}")
    }

    // Возвращает Flow с кэшированными данными для UI
    fun getCachedCars(): Flow<List<CarLists>> {
        return dao.getAllCars().map { entities ->
            entities.map {
                CarLists(
                    car_id = it.car_id,
                    Марка = it.Марка,
                    Модель = it.Модель,
                    Год_выпуска = it.Год_выпуска,
                    Цена_за_сутки = it.Цена_за_сутки,
                    Коробка_передач = it.Коробка_передач,
                    Местоположение = it.Местоположение,
                    imageUrls = it.imageUrls,
                    Владелец = it.Владелец,
                    Описание = it.Описание,
                    Доступность = it.Доступность,
                    updated_at = LocalDateTime.parse(it.updated_at)
                )
            }
        }.onEach { cars ->
            Log.d("CarREpository","Кеширование машины: ${cars.size} Данные: $cars")
        }
    }

    // Выполняет синхронизацию с Supabase в фоновом режиме
    suspend fun syncWithSupabase() = withContext(Dispatchers.IO) {
        try {
            val lastUpdateTime = dao.getlastUpdateTime() ?: "1970-01-01T00:00:00Z"
            Log.d("CarRepository","Последнее время обновления: $lastUpdateTime")

            Log.d("CarRepository","ВЫполняется запрос к тадлице Машина, Изображение автомобиля")
            val supabaseCar = supabaseClient.from("Машина")
                .select(Columns.raw("*, Изображение_автомобиля(image_url)")) {
                    filter {
                        gt("updated_at", lastUpdateTime)
                    }
                }
                .decodeList<CarSupabaseRaw>()

            Log.d("CarRepository", "Получено ${supabaseCar.size} новых машин")

            val imageUrlsMap = mutableMapOf<String, List<String>>()
            supabaseCar.forEach { carData ->
                val imageUrls = carData.images.mapNotNull { image ->
                    try {
                        val bucketName = "carimage"
                        val filePath = image.image_url.substringAfter("public/$bucketName/")
                        Log.d("CarRepository", "Путь к файлу: $filePath")
                        val signedUrl = supabaseClient.storage.from(bucketName)
                            .createSignedUrl(filePath, expiresIn = 1.hours)
                        Log.d("CarRepository", "Подписанный URL: $signedUrl")
                        signedUrl
                    } catch (e: Exception) {
                        Log.e("CarRepository", "Ошибка генерации URL для ${carData.car_id}: ${e.message}")
                        null
                    }
                }
                imageUrlsMap[carData.car_id] = imageUrls
            }

            val entities = supabaseCar.map {
                CarEntity(
                    car_id = it.car_id,
                    Марка = it.Марка,
                    Модель = it.Модель,
                    Цена_за_сутки = it.Цена_за_сутки,
                    Описание = it.Описание ?: "",
                    Владелец = it.Владелец,
                    Год_выпуска = it.Год_выпуска,
                    Коробка_передач = it.Коробка_передач,
                    Местоположение = it.Местоположение,
                    Тип_кузова = it.Тип_кузова,
                    Доступность = it.Доступность,
                    imageUrls = imageUrlsMap[it.car_id] ?: emptyList(),
                    updated_at = it.updated_at.toString()
                )
            }
            if (entities.isNotEmpty()) {
                dao.deleteallCars()
                Log.d("CarRepository","Очищена таблица Cars")
                dao.insertCars(entities)
                Log.d("CarRepository","Вставленно ${entities.size} машин в Room")
                val localCars = dao.logCars()
                Log.d("CarRepository", "Вставлено ${entities.size} машин в Room, всего в базе ${localCars.size} машин")
            }
        } catch (e: Exception) {
            Log.e("CarRepository", "Ошибка синхронизации с Supabase: ${e.message}")
        }
    }


    suspend fun searchCars(query: String): Flow<List<CarLists>> = withContext(Dispatchers.IO) {
        val searchQuery = "%${query.lowercase()}%"
        dao.searсhCars(searchQuery).map { entities ->
            entities.map {
                CarLists(
                    car_id = it.car_id,
                    Марка = it.Марка,
                    Модель = it.Модель,
                    Год_выпуска = it.Год_выпуска,
                    Цена_за_сутки = it.Цена_за_сутки,
                    Коробка_передач = it.Коробка_передач,
                    Местоположение = it.Местоположение,
                    imageUrls = it.imageUrls,
                    Владелец = it.Владелец,
                    Описание = it.Описание,
                    Доступность = it.Доступность,
                    updated_at = LocalDateTime.parse(it.updated_at)
                )
            }
        }.onEach { cars ->
            Log.d("CarRepository","Релузьтаты поиска: ${cars.size}, Данные $cars")
        }
    }

    suspend fun clearCache() {
        dao.deleteallCars()
        Log.d("CarRepository", "Кэш очищен")
    }
}
