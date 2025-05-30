package com.example.demo123.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.Log.e
import androidx.room.Query
import com.example.demo123.CarLists
import com.example.demo123.CarSupabaseRaw
import com.example.demo123.EmbeddedCarImage
import com.example.demo123.MyApplication
import com.example.demo123.data.CarEntity
import com.example.demo123.models.CarList
import com.example.demo123.models.SupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlin.collections.mapNotNull
import kotlin.time.Duration.Companion.hours

data class EmbeddedCarImage(val car_id: String, val image_url: String)

class CarRepository(
    private val supabaseClient: SupabaseClient,
    private val carDao: CarDao
) {

    fun getAllCars(): Flow<List<CarLists>> {
        return carDao.getAllCars().map { entityList ->
            entityList.map { it.toCarLists() }
        }
    }
    fun CarEntity.toCarLists(): CarLists {
        return CarLists(
            car_id = this.car_id,
            Марка = this.Марка,
            Модель = this.Модель,
            Год_выпуска = this.Год_выпуска,
            Коробка_передач = this.Коробка_передач,
            Цена_за_сутки = this.Цена_за_сутки,
            Местоположение = this.Местоположение,
            imageUrls = this.imageUrls,
            Владелец = this.Владелец,
            Описание = this.Описание,
            Доступность = this.Доступность,
            updated_at = this.updated_at,
            Тип_кузова = this.Тип_кузова
        )
    }
    fun getAllCarsFlow(): Flow<List<CarLists>> {
        return carDao.getAllCars().map { entity ->
            entity.map { entity -> entity.toCarLists() }
        }
    }

    suspend fun fetchCarsFromSupabaseAndCache(){
        try {

            //загружаем данные из таблицы Машина
            val carsRaw = supabaseClient.from("Машина")
                .select(Columns.raw("*, Изображение_автомобиля(image_url)"))
                .decodeList<CarSupabaseRaw>()

            //Создаем карту для подписанных URL изображений
            val imageUrlsMap = mutableMapOf<String, List<String>>()
            for (car in carsRaw) {
                val imageResponse = supabaseClient.from("Изображение_автомобиля")
                    .select(Columns.raw("car_id, image_url")) {
                        filter { eq("car_id", car.car_id) }
                    }
                    .decodeList<EmbeddedCarImage>()

                val signedUrls = imageResponse.mapNotNull {
                    try {
                        val bucketName = "carimage"
                        val filePath = it.image_url.substringAfter("public/$bucketName")
                        supabaseClient.storage.from(bucketName)
                            .createSignedUrl(filePath, expiresIn = 1.hours)
                    } catch (e: Exception) {
                        null
                    }
                }
                imageUrlsMap[car.car_id] = signedUrls
            }

            val carEntities = carsRaw.map { car ->
                if (car.car_id == null || car.Модель == null || car.Марка == null || car.Цена_за_сутки == null ||
                    car.images == null || car.Тип_кузова == null || car.updated_at == null || car.Доступность == null ||
                    car.Описание == null || car.Владелец == null || car.Местоположение == null || car.Год_выпуска == null ||
                    car.Коробка_передач == null
                ) {
                    Log.w("CarREpository", "Пропущена машина с null полями $car")
                    CarEntity(
                        car_id = car.car_id,
                        Марка = car.Марка,
                        Модель = car.Модель,
                        Год_выпуска = car.Год_выпуска,
                        Коробка_передач = car.Коробка_передач,
                        Цена_за_сутки = car.Цена_за_сутки,
                        Местоположение = car.Местоположение,
                        imageUrls = car.images,
                        Владелец = car.Владелец,
                        Описание = car.Описание,
                        Доступность = car.Доступность,
                        updated_at = car.updated_at,
                        Тип_кузова = car.Тип_кузова
                    )
                } else {
                    CarEntity(
                        car_id = car.car_id,
                        Марка = car.Марка,
                        Модель = car.Модель,
                        Год_выпуска = car.Год_выпуска,
                        Коробка_передач = car.Коробка_передач,
                        Цена_за_сутки = car.Цена_за_сутки,
                        Местоположение = car.Местоположение,
                        imageUrls = car.images,
                        Владелец = car.Владелец,
                        Описание = car.Описание,
                        Доступность = car.Доступность,
                        updated_at = car.updated_at,
                        Тип_кузова = car.Тип_кузова
                    )
                }
            }

            carDao.deleteallCars()
            carDao.insertCars(carEntities)
            Log.d("CarRepository", "Загружено ${carEntities.size} машин в SQLite")
        }catch (e: Exception){
            Log.e("CarRepository","Ошибка загрузки из Supabase: ${e.message}", e)
        }
    }

    suspend fun searchCars(query: String): List<CarLists> {
        val words = query.trim().lowercase().split("\\s+".toRegex())
        return when (words.size) {
            1 -> carDao.searchByBrandOrModel(words[0], words[0])
            2 -> carDao.searchByBrandOrModel(words[0], words[1])
            else -> emptyList()
        }
    }

    suspend fun clearCache(){
        try {
            carDao.deleteallCars()
            Log.d("CarRepository","Локальный кеш очищен")
        }catch (e: Exception){
            Log.e("CarREpository","Ошибка очистки кеша: ${e.message}", e)
        }
    }

    suspend fun syncCars(context: Context){
        if (!isNetworkAvailable(context)){
            Log.e("CarRepository","Нет соединения с интернетом")
            return
        }
        try {
            //Получаем время последнего обновления
            val lastUpdatedTime = carDao.getlastUpdateTime() ?: "1970-01-01"
            Log.d("CarREpository","Время последнего обновления $lastUpdatedTime")

            //запрашиваем машины с updated_at > lastUpdatedTime
            val query = supabaseClient.from("Машина")
                .select(Columns.raw("*, Изображение_автомобиля(image_url)")) {
                    filter { gt("updated_at", lastUpdatedTime) }
                }
            val carsRaw = query.decodeList<CarSupabaseRaw>()
            Log.d("CarRepository","Supabase Raw вернул $carsRaw")

            //Загружаем подписанные URL для изображений
            val imageUrlsMap = mutableMapOf<String, List<CarLists>>()
            for (car in carsRaw) {
                val carsRaw = supabaseClient.from("Машина")
                    .select(Columns.raw("*, Изображение_автомобиля(image_url)"))
                    .decodeList<CarSupabaseRaw>()

                //Создаем карту для подписанных URL изображений
                val imageUrlsMap = mutableMapOf<String, List<String>>()
                for (car in carsRaw) {
                    val imageResponse = supabaseClient.from("Изображение_автомобиля")
                        .select(Columns.raw("car_id, image_url")) {
                            filter { eq("car_id", car.car_id) }
                        }
                        .decodeList<EmbeddedCarImage>()

                    val signedUrls = imageResponse.mapNotNull {
                        try {
                            val bucketName = "carimage"
                            val filePath = it.image_url.substringAfter("public/$bucketName")
                            supabaseClient.storage.from(bucketName)
                                .createSignedUrl(filePath, expiresIn = 1.hours)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    imageUrlsMap[car.car_id] = signedUrls
                }
                val carEntities = carsRaw.map { car ->
                    if (car.car_id == null || car.Модель == null || car.Марка == null || car.Цена_за_сутки == null ||
                        car.images == null || car.Тип_кузова == null || car.updated_at == null || car.Доступность == null ||
                        car.Описание == null || car.Владелец == null || car.Местоположение == null || car.Год_выпуска == null ||
                        car.Коробка_передач == null
                    ) {
                        Log.w("CarREpository", "Пропущена машина с null полями $car")
                        CarEntity(
                            car_id = car.car_id,
                            Марка = car.Марка,
                            Модель = car.Модель,
                            Год_выпуска = car.Год_выпуска,
                            Коробка_передач = car.Коробка_передач,
                            Цена_за_сутки = car.Цена_за_сутки,
                            Местоположение = car.Местоположение,
                            imageUrls = car.images,
                            Владелец = car.Владелец,
                            Описание = car.Описание,
                            Доступность = car.Доступность,
                            updated_at = car.updated_at,
                            Тип_кузова = car.Тип_кузова
                        )
                    } else {
                        CarEntity(
                            car_id = car.car_id,
                            Марка = car.Марка,
                            Модель = car.Модель,
                            Год_выпуска = car.Год_выпуска,
                            Коробка_передач = car.Коробка_передач,
                            Цена_за_сутки = car.Цена_за_сутки,
                            Местоположение = car.Местоположение,
                            imageUrls = car.images,
                            Владелец = car.Владелец,
                            Описание = car.Описание,
                            Доступность = car.Доступность,
                            updated_at = car.updated_at,
                            Тип_кузова = car.Тип_кузова
                        )
                    }
                }
                //вставляем новые данные
                if (carEntities.isNotEmpty()) {
                    carDao.insertCars(carEntities)
                    Log.d("CarRepository","Синхронизированно ${carEntities.size} машин")
                }else {
                    Log.e("CarRepository","Нет новых машин для синхронизации")
                }
            }
        }catch (e: Exception){
            Log.e("CarRepository","Ошибка синхронизации ${e.message}",e)
        }
    }

    //вспомогательная функция для проверкиинтернета

    private fun isNetworkAvailable(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
