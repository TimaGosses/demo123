package com.example.demo123.data

import android.app.Application
import android.util.Log
import com.example.demo123.MyApplication
import com.example.demo123.SupabaseCar
import io.github.jan.supabase.postgrest.from
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CarRepository(
    private val dao: CarDao,
    private val application: Application
) {
    val supabaseClient = (application as MyApplication).supabase

    //загрузка данных с учетом кеша
    suspend fun getCar(): List<CarEntity> = withContext(Dispatchers.IO) {
        val lastUpdateTime = dao.getlastUpdateTime() ?: "1970-01-01T00:00:00Z"

        //Запрашиваем только новые или обновленные машины
        val supabaseCar = supabaseClient.from("Машина")
            .select{
                filter {
                    gt("created_at", lastUpdateTime)
                }
            }
            .decodeList<SupabaseCar>()

        Log.d("CarRepository","Fetched ${supabaseCar.size} new car")

        //преобразуем в локальную модель и сохраняем
        val cars = supabaseCar.map {
            CarEntity(
                car_id = it.car_id,
                Марка = it.Марка,
                Модель = it.Модель,
                Цена_за_сутки = it.Цена_за_сутки,
                Описание = it.Описание,
                Владелец = it.Владелец,
                Год_выпуска = it.Год_выпуска,
                VIN = it.VIN,
                Коробка_передач = it.Коробка_передач,
                Местоположение = it.Местоположение,
                Тип_кузова = it.Тип_кузова,
                imagePath = it.imagePath,
                updated_at = it.updated_at,
            )
        }

        if (cars.isNotEmpty()){
            dao.insertCars(cars)
        }
        //возвращаем все машины из локальной базы
        dao.getAllCars().first().also {
            Log.d("Room", "Машины: ${it.size}")
        }
    }
}