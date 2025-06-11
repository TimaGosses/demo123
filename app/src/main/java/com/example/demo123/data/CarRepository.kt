package com.example.demo123.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.Log.e
import androidx.room.Query
import com.example.demo123.CarListes
import com.example.demo123.CarLists
import com.example.demo123.CarSupabaseRaw
import com.example.demo123.CarSupabaseRaws
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
import kotlinx.coroutines.delay
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

    fun getAllCars(): Flow<List<CarListes>> {
        return carDao.getAllCars().map { entityList ->
            entityList.map { it.toCarListes() }
        }
    }
    fun CarEntity.toCarListes(): CarListes {
        return CarListes(
            car_id = this.car_id,
            Марка = this.Марка,
            Модель = this.Модель,
            Год_выпуска = this.Год_выпуска,
            Коробка_передач = this.Коробка_передач,
            Название_коробки_передач = this.Название_коробки_передач ?: "",
            Цена_за_сутки = this.Цена_за_сутки,
            Местоположение = this.Местоположение,
            Название_города = this.Название_города ?: "",
            //Название_региона = this.Название_региона,
            Владелец = this.Владелец,
            Тип_кузова = this.Тип_кузова,
            Название_типа_кузова = this.Название_типа_кузова?: "",
            Доступность = this.Доступность,
            Описание = this.Описание,
            imageUrls = this.imageUrls,
            updated_at = this.updated_at
        )
    }
    fun getAllCarsFlow(): Flow<List<CarListes>> {
        return carDao.getAllCars().map { entity ->
            entity.map { entity -> entity.toCarListes() }
        }
    }

    suspend fun fetchCars() {
        try {
            val carsRaw = supabaseClient.from("Машина")
                .select(Columns.raw("""
                car_id, Марка, Модель, Год_выпуска, Владелец, Цена_за_сутки, Описание, Доступность, updated_at,
                Изображение_автомобиля(image_url),
                Коробка_передач_автомобиля!inner(id, Название_коробки_передач),
                Тип_кузова!inner(id, Название_типа_кузова),
                Местонахождение_автомобиля!Машина_Местоположение_fkey(id, Город!Местонахождение_автомоби_Город_fkey(id, Название_города))
            """.trimIndent()))
                .decodeList<CarSupabaseRaws>()
            Log.d("CarRepository", "получено: ${carsRaw.size} машин из Supabase")

            val carEntities = carsRaw.mapNotNull { car ->
                try {
                    // Получаем подписанные URL для изображений
                    val signedUrls = car.images.mapNotNull { image ->
                        try {
                            val bucketName = "carimage"
                            val filePath = image.image_url.substringAfter("public/$bucketName")
                            supabaseClient.storage.from(bucketName)
                                .createSignedUrl(filePath, expiresIn = 1.hours)
                        } catch (e: Exception) {
                            Log.e("CarRepository", "Ошибка создания Url для ${image.image_url}: ${e.message}")
                            null
                        }
                    }

                    CarEntity(
                        car_id = car.car_id,
                        Марка = car.Марка,
                        Модель = car.Модель,
                        Год_выпуска = car.Год_выпуска,
                        Коробка_передач = car.Коробка_передач.id,
                        Цена_за_сутки = car.Цена_за_сутки,
                        Местоположение = car.Город?.id ?: 0, // Используем id города или 0
                        Название_коробки_передач = car.Коробка_передач.Название_коробки_передач,
                        Название_города = car.Местонахождение_автомобиля?.Город?.Название_города?: "",
                        //Название_региона = car.Местонахождение_автомобиля?.Город?.Регион?.Название_региона,
                        imageUrls = signedUrls,
                        Владелец = car.Владелец,
                        Описание = car.Описание ?: "",
                        Доступность = car.Доступность ?: true,
                        updated_at = car.updated_at,
                        Тип_кузова = car.Тип_кузова.id,
                        Название_типа_кузова = car.Тип_кузова.Название_типа_кузова
                    )
                } catch (e: Exception) {
                    Log.w("CarRepository", "Ошибка обработки машины ${car.car_id}: ${e.message}")
                    null
                }
            }

            carDao.deleteallCars()
            carDao.insertCars(carEntities)
            Log.d("CarRepository", "Загружено ${carEntities.size} машин в SQLite")
        } catch (e: Exception) {
            Log.e("CarRepository", "Ошибка загрузки из Supabase: ${e.message}", e)
        }
    }

    suspend fun fetchCarsWithRetry(retries: Int = 3, delayMillis: Long = 1000): List<CarListes> {
        repeat(retries) { attempt ->
            try {
                val carsRaw = supabaseClient
                    .from("Машина")
                    .select(Columns.raw("""
                car_id, Марка, Модель, Год_выпуска, Владелец, Цена_за_сутки, Описание, Доступность, updated_at,
                Изображение_автомобиля(image_url),
                Коробка_передач_автомобиля!inner(id, Название_коробки_передач),
                Тип_кузова!inner(id, Название_типа_кузова),
                Местонахождение_автомобиля!Машина_Местоположение_fkey(id, Город!Местонахождение_автомоби_Город_fkey(id, Название_города))
            """.trimIndent())){
                        filter { gt("updated_at", "2025-06-01T06:38:00.752306") }
                    }
                    .decodeList<CarSupabaseRaws>()
                return carsRaw.map { car ->
                    val signedUrls = car.images.mapNotNull { image ->
                        try {
                            val bucketName = "carimage"
                            val filePath = image.image_url.substringAfter("public/$bucketName")
                            supabaseClient.storage.from(bucketName)
                                .createSignedUrl(filePath, expiresIn = 1.hours)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    CarListes(
                        car_id = car.car_id,
                        Марка = car.Марка,
                        Модель = car.Модель,
                        Год_выпуска = car.Год_выпуска,
                        Коробка_передач = car.Коробка_передач.id,
                        Цена_за_сутки = car.Цена_за_сутки,
                        Местоположение = car.Город?.id ?: 0, // Используем id города или 0
                        Название_коробки_передач = car.Коробка_передач.Название_коробки_передач,
                        Название_города = car.Местонахождение_автомобиля?.Город?.Название_города?: "",
                        //Название_региона = car.Местоположение.Город.Регион.Название_региона,
                        imageUrls = signedUrls,
                        Владелец = car.Владелец,
                        Описание = car.Описание ?: "",
                        Доступность = car.Доступность ?: true,
                        updated_at = car.updated_at,
                        Тип_кузова = car.Тип_кузова.id,
                        Название_типа_кузова = car.Тип_кузова.Название_типа_кузова
                    )
                }
            } catch (e: Exception) {
                if (attempt == retries - 1) throw e
                Log.w("CarRepository", "Попытка ${attempt + 1} не удалась, повтор через $delayMillis мс")
                delay(delayMillis)
            }
        }
        throw Exception("Не удалось выполнить запрос после $retries попыток")
    }

    suspend fun searchCars(query: String): List<CarListes> {
        val words = query.trim().lowercase().split("\\s+".toRegex())
        return when (words.size) {
            1 -> carDao.searchByBrandOrModel(words[0], words[0]).map { it.toCarListes() }
            2 -> carDao.searchByBrandOrModel(words[0], words[1]).map { it.toCarListes() }
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

    suspend fun syncCars(context: Context) {
        if (!isNetworkAvailable(context)) {
            Log.e("CarRepository", "Нет соединения с интернетом")
            return
        }
        try {
            val lastUpdatedTime = carDao.getlastUpdateTime() ?: "1970-01-01"
            Log.d("CarRepository", "Время последнего обновления $lastUpdatedTime")

            val carsRaw = supabaseClient.from("Машина")
                .select(Columns.raw("""
                car_id, Марка, Модель, Год_выпуска, Владелец, Цена_за_сутки, Описание, Доступность, updated_at,
                Изображение_автомобиля(image_url),
                Коробка_передач_автомобиля!inner(id, Название_коробки_передач),
                Тип_кузова!inner(id, Название_типа_кузова),
                Местонахождение_автомобиля!Машина_Местоположение_fkey(id, Город!Местонахождение_автомоби_Город_fkey(id, Название_города))
            """.trimIndent())) {
                    filter { gt("updated_at", lastUpdatedTime) }
                }
                .decodeList<CarSupabaseRaws>()
            Log.d("CarRepository", "Supabase Raw вернул ${carsRaw.size} машин")

            val carEntities = carsRaw.mapNotNull { car ->
                try {
                    val signedUrls = car.images.mapNotNull { image ->
                        try {
                            val bucketName = "carimage"
                            val filePath = image.image_url.substringAfter("public/$bucketName")
                            supabaseClient.storage.from(bucketName)
                                .createSignedUrl(filePath, expiresIn = 1.hours)
                        } catch (e: Exception) {
                            Log.e("CarRepository", "Ошибка создания URL: ${e.message}")
                            null
                        }
                    }
                    if (car.car_id.isBlank() || car.Модель.isBlank() || car.Марка.isBlank()) {
                        Log.w("CarRepository", "Пропущена машина с пустыми полями: $car")
                        return@mapNotNull null
                    }
                    CarEntity(
                        car_id = car.car_id,
                        Марка = car.Марка,
                        Модель = car.Модель,
                        Год_выпуска = car.Год_выпуска,
                        Коробка_передач = car.Коробка_передач.id,
                        Цена_за_сутки = car.Цена_за_сутки,
                        Местоположение = car.Местонахождение_автомобиля?.id ?: 0,
                        Название_коробки_передач = car.Коробка_передач.Название_коробки_передач,
                        Название_города = car.Местонахождение_автомобиля?.Город?.Название_города ?: "",
                        imageUrls = signedUrls,
                        Владелец = car.Владелец,
                        Описание = car.Описание ?: "",
                        Доступность = car.Доступность ?: true,
                        updated_at = car.updated_at,
                        Тип_кузова = car.Тип_кузова.id,
                        Название_типа_кузова = car.Тип_кузова.Название_типа_кузова
                    )
                } catch (e: Exception) {
                    Log.e("CarRepository", "Ошибка обработки машины ${car.car_id}: ${e.message}", e)
                    null
                }
            }
            if (carEntities.isNotEmpty()) {
                carDao.insertCars(carEntities)
                Log.d("CarRepository", "Синхронизировано ${carEntities.size} машин")
            } else {
                Log.d("CarRepository", "Нет новых машин для синхронизации")
            }
        } catch (e: Exception) {
            Log.e("CarRepository", "Ошибка синхронизации: ${e.message}", e)
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




    /*val carEntities = carsRaw.mapNotNull { car ->
        try {
            val bucketName = "carimage"
            val signedUrls = car.images.mapNotNull { image ->
                try {
                    val filePath = when {
                        image.image_url.startsWith("https://twkqrtcvsuwoyrbleuiu.supabase.co/storage/v1/object/public/$bucketName/") ->
                            image.image_url.removePrefix("https://twkqrtcvsuwoyrbleuiu.supabase.co/storage/v1/object/public/$bucketName/")
                        image.image_url.startsWith("public/$bucketName/") ->
                            image.image_url.removePrefix("public/$bucketName/")
                        image.image_url.startsWith("$bucketName/") ->
                            image.image_url.removePrefix("$bucketName/")
                        else -> image.image_url //если не распознан префикс, то используем локальный
                    }.trim('/')
                    Log.d("CarRepository","Генерация URL: image_url = ${image.image_url}, filePath = $filePath")
                    val signedUrl = supabaseClient.storage.from(bucketName)
                        .createSignedUrl(filePath, expiresIn = 5.hours)
                    Log.d("CarRepository","Сгенерирован signedUrl $signedUrl")
                    signedUrl*/