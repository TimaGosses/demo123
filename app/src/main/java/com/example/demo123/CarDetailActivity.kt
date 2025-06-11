package com.example.demo123

import android.content.Context
import android.os.Build

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.demo123.databinding.ActivityCarDetailBinding
import com.google.gson.Gson
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import kotlin.time.Duration.Companion.seconds


class CarDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var imageAdapter: ImageAdapter
    private val supabaseClient by lazy { (application as MyApplication).supabase }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CarDetailActivity", "Intent extras: ${intent.extras?.keySet()?.joinToString() ?: "Пусто"}")

        val car = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("CarDetail", CarListes::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("CarDetail") as? CarListes
        }

        if (car == null) {
            Log.e("CarDetailActivity", "Ошибка: данные CarListes не передались")
            Toast.makeText(this, "Ошибка загрузки автомобиля", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("CarDetailActivity", "Получены данные: $car")

        // Загружаем данные владельца
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userData = supabaseClient
                    .from("Пользователь")
                    .select{
                        filter { eq("user_id", car.Владелец) }
                    }

                    .decodeSingle<UserData>()
                withContext(Dispatchers.Main) {
                    binding.textOwnerName.text = "${userData.Name} ${userData.Middle_name}"
                    binding.buttonCall.setOnClickListener {
                        userData.Number_phone?.let { phone ->
                            Log.d("CarDetailActivity", "Попытка звонка на номер $phone")
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            startActivity(intent)
                        } ?: Log.d("CarDetailActivity", "Номер телефона отсутствует")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CarDetailActivity", "Ошибка загрузки данных владельца: ${e.message}")
                    Toast.makeText(this@CarDetailActivity, "Ошибка загрузки данных владельца", Toast.LENGTH_SHORT).show()
                }
            }
        }

        imageAdapter = ImageAdapter(useUrls = true)
        binding.viewPagerImages.adapter = imageAdapter
        binding.dotsIndicator.setViewPager2(binding.viewPagerImages)
        binding.buttonback.setOnClickListener {
            val intent = Intent(this@CarDetailActivity, ListCar::class.java)
            startActivity(intent)
            finish()
        }

        loadImagesToFiles(car.imageUrls)
        loadOwnerData(car.Владелец)
        Log.d("CarDetailActivity", "Владелец из car: ${car.Владелец} Длина: ${car.Владелец.length}")

        binding.textInfo.text = "Характеристики"
        binding.textBrandModel.text = "${car.Марка} ${car.Модель}"
        binding.textYear.text = "${car.Год_выпуска} года"
        binding.textTransmission.text = "Коробка передач: ${car.Название_коробки_передач}"
        binding.textPrice.text = "${car.Цена_за_сутки} руб/сутки"
        binding.textLocation.text = "Местоположение: ${car.Название_города}"
        binding.textBodyType.text = "Тип кузова: ${car.Название_типа_кузова}"
        binding.textAvailability.text = if (car.Доступность == true) "Доступно" else "Недоступно"
        binding.textDescription.text = car.Описание ?: "Описание отсутствует"
    }

    private suspend fun refreshImageUrl(imageUrl: String): String {
        val fileName = imageUrl.substringAfterLast("/").substringBefore("?").trim()
        Log.e("CarDetailActivity", "refreshImageUrl: imageUrl = :$imageUrl, fileName = $fileName")
        return try {
            supabaseClient.storage
                .from("carimage")
                .createSignedUrl(fileName, expiresIn = 300.seconds)
        } catch (e: Exception) {
            Log.e("CarDetailActivity", "Ошибка создания подписанного URL для $fileName: ${e.message}")
            throw e
        }
    }

    private fun loadImagesToFiles(imageUrls: List<String>) {
        CoroutineScope(Dispatchers.Main).launch {
            if (imageUrls.isEmpty()) {
                Log.d("CarDetailActivity", "Список URL пуст")
                Toast.makeText(this@CarDetailActivity, "Изображения отсутствуют", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (!isNetworkAvailable()) {
                Log.e("CarDetailActivity", "Нет интернет-соединения")
                Toast.makeText(this@CarDetailActivity, "Нет интернета", Toast.LENGTH_SHORT).show()
                return@launch
            }
            Log.d("CarDetailActivity", "Список URL изображений: $imageUrls")
            imageUrls.map { url ->
                async(Dispatchers.IO) {
                    try {
                        val validUrl = refreshImageUrl(url)
                        validUrl
                    } catch (e: Exception) {
                        Log.e("CarDetailActivity", "Ошибка обработки URL: $url: ${e.message}")
                        null
                    }
                }
            }.awaitAll().filterNotNull().forEach { validUrl ->
                imageAdapter.addUrl(validUrl)
                Log.d("CarDetailActivity", "Добавлен URL: $validUrl")
            }
            imageAdapter.notifyDataSetChanged()

            binding.viewPagerImages.adapter = imageAdapter
            binding.dotsIndicator.setViewPager2(binding.viewPagerImages)
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    private fun getCachedOwner(ownerId: String): UserData? {
        val sharedPrefs = getSharedPreferences("owner_cache", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("owner_$ownerId", null) ?: return null
        return try {
            Gson().fromJson(json, UserData::class.java)
        } catch (e: Exception) {
            Log.e("CarDetailActivity", "Ошибка парсинга кэша: ${e.message}")
            null
        }
    }

    private fun cacheOwner(userData: UserData) {
        val sharedPrefs = getSharedPreferences("owner_cache", Context.MODE_PRIVATE)
        val json = Gson().toJson(userData)
        sharedPrefs.edit().putString("owner_${userData.user_id}", json).apply()
    }

    private fun loadOwnerData(ownerId: String) {
        // Реализуй метод, если нужно
    }
}