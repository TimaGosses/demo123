package com.example.demo123

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo123.GetCar
import com.example.demo123.databinding.ActivityListCarBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

    private lateinit var recycleView: RecyclerView
    private lateinit var carAdapter: CarAdapter
    private var carDataList = mutableListOf<CarLists>()
    private lateinit var binding: ActivityListCarBinding
    private lateinit var buttonGetCarList: Button
    private lateinit var buttonRegister: Button






class ListCar : AppCompatActivity() {
    private lateinit var binding: ActivityListCarBinding
    private lateinit var carAdapter: CarAdapter
    private var carDataList = mutableListOf<CarLists>()
    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка RecyclerView
        binding.carRecyclerView.layoutManager = LinearLayoutManager(this)
        carAdapter = CarAdapter(emptyList()) { car ->
            val intent = Intent(this, MyCarsActivity::class.java)
            startActivity(intent)
        }
        binding.carRecyclerView.adapter = carAdapter

        // Отключаем кнопку поиска до загрузки данных
        binding.buttonSearch.isEnabled = false

        // Настройка кнопки добавления автомобиля
        binding.buttonGetCarList.setOnClickListener {
            val intent = Intent(this, GetCar::class.java)
            startActivity(intent)
        }
        binding.buttonRegister.setOnClickListener {
            val intent = Intent(this@ListCar, ProfileActivity::class.java)
            startActivity(intent)

        }

        // Поиск
        binding.buttonSearch.setOnClickListener {
            if (!isDataLoaded) {
                Toast.makeText(this, "Данные еще загружаются", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val query = binding.editTextSearch.text.toString().trim().lowercase()
            if (query.isEmpty()) {
                carAdapter.updateCars(carDataList)
                return@setOnClickListener
            }

            // Разделяем запрос на слова
            val words = query.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            val filteredCars = when (words.size) {
                1 -> {
                    // Одно слово: искать по Марка ИЛИ Модель
                    val singleQuery = words[0]
                    carDataList.filter {
                        it.Марка.lowercase().contains(singleQuery) ||
                                it.Модель.lowercase().contains(singleQuery)
                    }
                }
                2 -> {
                    // Два слова: первое для Марка, второе для Модель
                    val brandQuery = words[0]
                    val modelQuery = words[1]
                    carDataList.filter {
                        it.Марка.lowercase().contains(brandQuery) &&
                                it.Модель.lowercase().contains(modelQuery)
                    }
                }
                else -> {
                    // Более двух слов: использовать первые два или показать пустой список
                    emptyList()
                }
            }

            Log.d("ListCar", "Поиск: query=$query, words=$words, filteredCars.size=${filteredCars.size}")
            carAdapter.updateCars(filteredCars)
            if (filteredCars.isEmpty()) {
                Toast.makeText(this, "Автомобили не найдены", Toast.LENGTH_SHORT).show()
            }
        }

        // Загрузка данных
        checkAuthAndFetchData()
    }

    private fun checkAuthAndFetchData(){
        val supabaseClient = (application as MyApplication).supabase
        lifecycleScope.launch {
            try {
                //Проверка текущего пользователя
                val currentUser = supabaseClient.auth.currentUserOrNull()
                Log.d("ListCar","Текущий пользователь: ${currentUser?.email ?: "null"}")

                if (currentUser == null){
                    //Попытка восстановления сессии
                    try {
                        supabaseClient.auth.retrieveUserForCurrentSession()
                        Log.d("ListCar","Пользователь восстановлен ${currentUser?.email}")
                    }catch (e: Exception){
                        Log.e("ListCar","Ошибка восстановления сессии ${e.message}")
                        Toast.makeText(this@ListCar,"Требуется повторная авторизация",Toast.LENGTH_SHORT).show()
                        //перенаправление на экран авторизации
                        val intent = Intent(this@ListCar, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        return@launch
                    }
                }
                //если пользователь авторизован
                if (supabaseClient.auth.currentUserOrNull() != null){
                    fetchCarsFromSupabase()
                }else{
                    Log.e("ListCar","Пользователь не аутентифицирован после попытки восстановления сессии")
                    Toast.makeText(this@ListCar,"Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ListCar, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }catch (e: Exception){
                Log.e("ListCar","Ошибка проверки авторизации ${e.message}")
                Toast.makeText(this@ListCar,"Ошибка авторизации ${e.message}",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCarsFromSupabase() {
        val supabaseClient = (application as MyApplication).supabase
        lifecycleScope.launch {
            try {
                val carsRaw = supabaseClient.from("Машина")
                    .select(Columns.raw("*, Изображение_автомобиля(image_url)"))
                    .decodeList<CarSupabaseRaw>()

                Log.d("ListCar", "Загружено автомобилей: ${carsRaw.size}")
                carsRaw.forEach { car ->
                    Log.d("ListCar", "Автомобиль: ${car.Марка}, ${car.Модель}, ${car.car_id}")
                }

                val imageUrlsMap = mutableMapOf<String, List<String>>()
                carsRaw.forEach { carData ->
                    val imageResponse = supabaseClient.from("Изображение_автомобиля")
                        .select(Columns.raw("car_id, image_url")) {
                            filter { eq("car_id", carData.car_id) }
                        }
                        .decodeList<EmbeddedCarImage>()
                    Log.d("ListCar", "Для car_id=${carData.car_id} найдено фотографий: ${imageResponse.size}")

                    val imageUrls = imageResponse.mapNotNull { image ->
                        try {
                            val bucketName = "carimage"
                            val filePath = image.image_url.substringAfter("public/$bucketName/")
                            Log.d("ListCar", "Путь к файлу: $filePath")
                            val signedUrl = supabaseClient.storage
                                .from(bucketName)
                                .createSignedUrl(filePath, expiresIn = 1.hours)
                            Log.d("ListCar", "Подписанный URL: $signedUrl")
                            signedUrl
                        } catch (e: Exception) {
                            Log.e("ListCar", "Ошибка генерации URL: ${e.message}")
                            null
                        }
                    }
                    imageUrlsMap[carData.car_id] = imageUrls
                }

                carDataList.clear()
                carDataList.addAll(carsRaw.map { carSupabaseRaw ->
                    val imageUrls = imageUrlsMap[carSupabaseRaw.car_id] ?: emptyList()
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
                })
                isDataLoaded = true
                binding.buttonSearch.isEnabled = true
                carAdapter.updateCars(carDataList)
                binding.carRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("ListCar", "Ошибка загрузки авто: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(this@ListCar, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
                isDataLoaded = true
                binding.buttonSearch.isEnabled = true
                binding.carRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}