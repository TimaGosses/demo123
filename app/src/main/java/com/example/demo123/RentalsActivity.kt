package com.example.demo123

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo123.databinding.ActivityRentalsBinding
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RentalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRentalsBinding
    private lateinit var carAdapter: CarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /*//Настройка RicyclerView
        binding.recycleViewCars.layoutManager = LinearLayoutManager(this)
        carAdapter = CarAdapter(emptyList())
        binding.recycleViewCars.adapter = carAdapter

         */

        //Загрузка данных
        loadRentaCars()
    }
    private fun loadRentaCars(){
        val supabaseClient = (application as MyApplication).supabase

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("RentalsActivity","Начало загрузки машин")

                //Запрос к Supabase с фильтрами по доступности
                val carResponse = withContext(Dispatchers.Main){
                    supabaseClient.from("Машина")
                        .select(columns = Columns.raw("""
                            car_id, Марка, Модель, Год_выпуска, Цена_за_сутки, Доступность, Местоположение, 
                            Описание, Владелец, VIN, Коробка_передач, Тип_кузова, Класс
                            Коробка_передач!left(Название),
                            Местоположение!left(Регион, Город)
                            (Изображение автомобиля)!left(image_url, user_id, car_id, id)
                        """.trimIndent())) {
                            filter {
                                eq("Доступность", true)  //фильтр на доступные автомобили
                            }
                        }
                        .decodeList<CarResponseWithJoins>()
                }
                Log.d("RentailsActivity","Загружено: ${carResponse.size} cars: $carResponse")

                //Преобразование CarResponseWithJoins в Car
               /* val cars = carResponse.map { carResponse ->
                    Car(
                        car_id = carResponse.car_id,
                        Марка = carResponse.марка,
                        Модель = carResponse.модель,
                        Год_выпуска = carResponse.год_выпуска,
                        Цена_за_сутки = carResponse.цена_за_сутки,
                        Владелец = carResponse.владелец,
                        Коробка_передач = carResponse.коробка_передач?.название,
                        Город = carResponse.местоположение?.город,
                        Регион = carResponse.местоположение?.регион,
                        Тип_кузова = carResponse.тип_кузова,
                        Класс = carResponse.класс_автомобиля?.название,
                        Доступность = carResponse.доступность,
                        VIN = carResponse.vin,
                        Описание = carResponse.описание,
                        imageUrls = carResponse.изображения.map { image ->
                            supabaseClient.storage.from("carimage").publicUrl(image.url_изображения)
                        }
                    )
                }
                //обновление адаптера
                carAdapter = CarAdapter(cars)
                binding.recycleViewCars.adapter = carAdapter
                carAdapter.notifyDataSetChanged()*/
            }catch (e: Exception){

            }
        }
    }
}