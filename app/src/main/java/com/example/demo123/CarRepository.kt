package com.example.demo123

import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


/*class CarRepository(private val app: MyApplication){
    suspend fun getCar(): List<Car> = withContext(Dispatchers.IO) {
        try {
            val carResponse = app.supabase.postgrest["Машина"]
                .select()
                .decodeList<CarResponse>()

            val cars = carResponse.map {carResponse ->
                val images = app.supabase.postgrest["Изображение автомомбиля"]
                    .select(Columns.raw("*")){
                        filter{ eq("car_id", carResponse.carId)}
                    }
                    .decodeList<CarImage>()
                Car(
                    car_id = carResponse.carId,
                    Марка = carResponse.Марка,
                    Модель = carResponse.Модель,
                    Год_выпуска = carResponse.Год_выпуска,
                    Цена_за_сутки = carResponse.Цена_за_сутки,
                    Коробка_передач = carResponse.Коробка_передач,
                    Местоположение = carResponse.Местоположение,
                    imageUrls = images.map { it.image_url },
                    Владелец = carResponse.Владелец,
                )
            }
            cars
        }catch (e: Exception){
            android.util.Log.e("CarRepository","Error loading cars: ${e.message}")
            emptyList()
        }

    }
}*/