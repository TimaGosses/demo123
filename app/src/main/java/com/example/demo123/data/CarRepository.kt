package com.example.demo123.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.Log.e
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
import kotlinx.coroutines.flow.firstOrNull
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


    //Получаем все автомобили
    suspend fun getAllCarsRaw(): List<CarEntity> {
        val cars = dao.getAllCars().firstOrNull() ?: emptyList()
        Log.d("CarRepository","Количество ${cars.size} в таблице SQlite ")
        return cars
    }

    //Получить автомобиль по ID
    suspend fun getCarById(carId: String): CarEntity? {
        return dao.getCarById(carId)
    }

    //Вставить автомобили в SQlite
    suspend fun insertCars(cars: List<CarEntity>){
        dao.insertCars(cars)
    }

    //снихронизация с данными из supabase
    suspend fun syncCars(context: Context){
        if (isNetworkAvaliable(context)) {
            try {
                val lastUpdateTime = dao.getlastUpdateTime() ?: 0L
                Log.d("CarRepository","Время последнего обновления $lastUpdateTime")

                //Загружаем данные если база данных пуста
                val query = if (lastUpdateTime == 0L) {
                    Log.d("CarRepository","Загрузка всех машин из Supabase")
                    supabaseClient.from("Машина").select()
                }else {
                    Log.d("CarREpository","Загрузка более новых машин с датой последнего обновления > $lastUpdateTime")
                    supabaseClient.from("Машина").select{
                        filter { eq("last_updated","gt.$lastUpdateTime") }
                    }
                }
                val newCarsRaw = query.decodeList<CarSupabaseRaw>()
                Log.d("CarREpository","Машины из supabase: $newCarsRaw")

                val newCars = newCarsRaw.mapNotNull { raw ->
                    if (raw.car_id == null || raw.Марка == null || raw.Модель  == null || raw.Год_выпуска == null || raw.Цена_за_сутки == null || raw.Коробка_передач == null ||
                        raw.Местоположение == null || raw.Тип_кузова == null || raw.updated_at == null || raw.Описание == null || raw.images == null || raw.Доступность == null ||
                        raw.Владелец == null) {
                        Log.d("CarRepository","Пропустили одно из полей машины $raw")
                        null
                    }else{
                        CarEntity(
                            car_id = raw.car_id,
                            Марка = raw.Марка,
                            Модель = raw.Модель,
                            Цена_за_сутки = raw.Цена_за_сутки,
                            Тип_кузова = raw.Тип_кузова,
                            Коробка_передач = raw.Коробка_передач,
                            Местоположение = raw.Местоположение,
                            Описание = raw.Описание,
                            imageUrls = raw.images,
                            updated_at = raw.updated_at,
                            Доступность = raw.Доступность,
                            Владелец = raw.Владелец,
                            Год_выпуска = raw.Год_выпуска
                        )
                    }
                }
                if (newCars.isNotEmpty()){
                    Log.d("CarRepository","Загружено ${newCars.size} машин в Sqlite $newCars")
                    dao.insertCars(newCars)
                }else {
                    Log.d("CarRepository","новые машины не загружены")
                }
            }catch (e: Exception) {
                Log.d("CarRepository","Ошибка загрузки ${e.message}",e)
            }
        }else {
            Log.e("CarREpository","Нет соединения с интернетом, попробуйте позже")
        }
    }

    suspend fun clearCache() {
        dao.deleteallCars()
        Log.d("CarRepository", "Кэш очищен")
    }

    private fun isNetworkAvaliable(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
    }

}
