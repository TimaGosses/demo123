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
            intent.getSerializableExtra("CarDetail", CarLists::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("CarDetail") as? CarLists
        }

        if (car == null) {
            Log.e("CarDetailActivity", "Ошибка: данные CarLists не передались")
            Toast.makeText(this, "Ошибка загрузки автомобиля", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("CarDetailActivity", "Получены данные: $car")

        imageAdapter = ImageAdapter(useUrls = true)
        binding.viewPagerImages.adapter = imageAdapter
        binding.dotsIndicator.setViewPager2(binding.viewPagerImages)

        loadImagesToFiles(car.imageUrls)
        loadOwnerData(car.Владелец)
        Log.d("CarDetailActivity","Владелец из car: ${car.Владелец}, Длина: ${car.Владелец.length}")

        binding.textBrandModel.text = "${car.Марка} ${car.Модель}"
        binding.textYear.text = "${car.Год_выпуска}"
        binding.textTransmission.text = "${car.Коробка_передач}"
        binding.textPrice.text = "${car.Цена_за_сутки}"
        binding.textLocation.text = "${car.Местоположение}"
        binding.textBodyType.text = "${car.Тип_кузова}"
        binding.textAvailability.text = if (car.Доступность == true) "Доступно" else "Недоступно"
        binding.textDescription.text = car.Описание ?: "Описание отсутствует"
    }

    private suspend fun refreshImageUrl(imageUrl: String): String {
        val fileName = imageUrl.substringAfterLast("/")
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
            Log.d("CarDetailActivity", "Адаптер обновлён, размер списка: ${imageAdapter.itemCount}, URLs: ${imageAdapter.getUrls()}")
            if (imageAdapter.itemCount == 0) {
                Log.e("CarDetailActivity", "Нет изображений для автомобиля")
                Toast.makeText(this@CarDetailActivity, "Изображения не загружены", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadOwnerData(ownerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (ownerId.isBlank()) {
                    withContext(Dispatchers.Main) {
                        Log.e("CarDetailActivity", "Владелец не указан для автомобиля")
                        Toast.makeText(this@CarDetailActivity, "Владелец не указан", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                Log.d("CarDetailActivity", "ownerId: '$ownerId' (длина: ${ownerId.length})")
                val cachedOwner = getCachedOwner(ownerId)
                if (cachedOwner != null) {
                    withContext(Dispatchers.Main) {
                        Log.d("CarDetailActivity", "Используются кэшированные данные владельца: $cachedOwner")
                        binding.textOwnerName.text = "${cachedOwner.Name} ${cachedOwner.Middle_name}"
                        binding.buttonCall.setOnClickListener {
                            cachedOwner.Number_phone?.let { phone ->
                                Log.d("CarDetailActivity", "Попытка звонка на номер $phone")
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                startActivity(intent)
                            } ?: Log.d("CarDetailActivity", "Номер телефона отсутствует")
                        }
                    }
                    return@launch
                }
                if (!isNetworkAvailable()) {
                    withContext(Dispatchers.Main) {
                        Log.e("CarDetailActivity", "Нет интернет-соединения")
                        Toast.makeText(this@CarDetailActivity, "Нет интернета", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                Log.d("CarDetailActivity", "Запрос данных владельца для user_id: $ownerId")
                val response = supabaseClient
                    .from("Пользователь") // Попробуйте "пользователь", если нужно
                    .select {
                        filter { eq("user_id", ownerId) }
                    }
                val rawData = response.data
                Log.d("CarDetailActivity", "Сырой ответ от Supabase (data): $rawData")
                Log.d("CarDetailActivity", "HTTP статус: ${response}")
                val userDataList = response.decodeList<UserData>()
                Log.d("CarDetailActivity", "Получено ${userDataList.size} записей для user_id: $ownerId, данные: $userDataList")
                withContext(Dispatchers.Main) {
                    if (userDataList.isNotEmpty()) {
                        val userData = userDataList.first()
                        cacheOwner(userData)
                        binding.textOwnerName.text = "${userData.Surname}, ${userData.Name}, ${userData.Middle_name}"
                        binding.buttonCall.setOnClickListener {
                            userData.Number_phone?.let { phone ->
                                Log.d("CarDetailActivity", "Попытка звонка на номер $phone")
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                startActivity(intent)
                            } ?: Log.d("CarDetailActivity", "Номер телефона отсутствует")
                        }
                    } else {
                        Log.e("CarDetailActivity", "Данные владельца не найдены для user_id: $ownerId")
                        Toast.makeText(this@CarDetailActivity, "Данные владельца не найдены", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CarDetailActivity", "Ошибка загрузки данных владельца: ${e.message}", e)
                    Toast.makeText(this@CarDetailActivity, "Ошибка загрузки данных владельца", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}