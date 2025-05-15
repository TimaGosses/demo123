package com.example.demo123

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo123.databinding.ActivityListCarBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

private lateinit var recycleView: RecyclerView
private lateinit var carAdapter: CarAdapter
private var carList = mutableListOf<Car>()
private lateinit var binding: ActivityListCarBinding

class ListCar : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListCarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_list_car)

        //настройка RecycleView
        binding.carRecyclerView.layoutManager = LinearLayoutManager(this@ListCar)
        carAdapter = CarAdapter(emptyList()) { car ->
            val intent = Intent(this@ListCar, MyCarsActivity::class.java)
            startActivity(intent)
        }
        binding.carRecyclerView.adapter = carAdapter

        fetchCarsFromSupabase()

    }

    private fun fetchCarsFromSupabase() {
        val supabaseClient = (application as MyApplication).supabase
        lifecycleScope.launch {
            try {
                // Получаем данные автомобилей из таблицы Машина, включая вложенные изображения
                val carsRaw = supabaseClient.from("Машина")
                    .select(Columns.raw("*, Изображение_автомобиля(image_url)"))
                    .decodeList<CarSupabaseRaw>()

                Log.d("ListCar", "Загружено автомобилей: ${carsRaw.size}")
                carsRaw.forEach { car ->
                    Log.d("ListCar", "Автомобиль: ${car.Марка}, ${car.Модель}, ${car.car_id}")
                }

                // Создаём мапу для хранения imageUrls для каждого car_id
                val imageUrlsMap = mutableMapOf<String, List<String>>()

                // Загружаем изображения для всех автомобилей и сохраняем их в мапу
                carsRaw.forEach { carData ->
                    val imageResponse = supabaseClient.from("Изображение_автомобиля")
                        .select(Columns.raw("car_id, image_url")) {
                            filter {
                                eq("car_id", carData.car_id)
                            }
                        }
                        .decodeList<CarImageS>()
                    Log.d(
                        "ListCar",
                        "Для car_id=${carData.car_id} найдено фотографий: ${imageResponse.size}"
                    )

                    // Генерируем подписанные URL для каждого изображения
                    val imageUrls = imageResponse.mapNotNull { image ->
                        try {
                            val backetName = "carimage"
                            val filePath = image.image_url.substringAfter("public/$backetName/")
                            Log.d("ListCar", "Путь к файлу: $filePath")

                            val signedUrl = supabaseClient.storage
                                .from(backetName)
                                .createSignedUrl(filePath, expiresIn = 1.hours)
                            Log.d("ListCar", "Подписанный URL фотографии: $signedUrl")
                            signedUrl
                        } catch (e: Exception) {
                            Log.e(
                                "ListCar",
                                "Ошибка генерации подписанного URL для ${image.image_url}: ${e.message}"
                            )
                            null
                        }
                    }
                    imageUrlsMap[carData.car_id] = imageUrls
                }

                // Создаём carDataList, используя мапу imageUrlsMap
                val carDataList = carsRaw.map { carSupabaseRaw ->
                    val imageUrls = imageUrlsMap[carSupabaseRaw.car_id]
                        ?: emptyList() // Получаем imageUrls для текущего car_id
                    CarLists(
                        car_id = carSupabaseRaw.car_id,
                        Марка = carSupabaseRaw.Марка,
                        Модель = carSupabaseRaw.Модель,
                        Год_выпуска = carSupabaseRaw.Год_выпуска,
                        Коробка_передач = carSupabaseRaw.Коробка_передач,
                        Цена_за_сутки = carSupabaseRaw.Цена_за_сутки,
                        Местоположение = carSupabaseRaw.Местоположение,
                        imageUrls = imageUrls,
                        Владелец = carSupabaseRaw.Владелец,
                        Описание = carSupabaseRaw.Описание ?: ""
                    )
                }

                // Обновляем адаптер ОДИН РАЗ после загрузки всех данных
                carAdapter.updateCars(carDataList)
                binding.carRecyclerView.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("ListCar", "Ошибка загрузки авто: ${e.message}", e)
                Toast.makeText(this@ListCar, "Ошибка загрузки автомобилей", Toast.LENGTH_SHORT)
                    .show()
                binding.carRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}
