package com.example.demo123

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demo123.databinding.ActivityCarDetailBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class CarDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var imageAdapter: ImageAdapter
    private val supabaseClient by lazy { (application as MyApplication).supabase }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CarDetailActivity","Intent extras: ${intent.extras?.keySet()?.joinToString() ?: "Пусто"}")

        //безопасное получение CarLists
        val car = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            intent.getSerializableExtra("CarDetail") as? CarLists
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("CarDetail") as? CarLists
        }

        if (car == null) {
            Log.d("CarDetailActivity", "Ошибка: данные CarLists не передались")
            Toast.makeText(this@CarDetailActivity,"Ошибка загрузки автомобиля", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("CarDetailActivity", "Получены данные: $car")

        // Настройка карусели фотографий
        imageAdapter = ImageAdapter()
        binding.viewPagerImages.adapter = imageAdapter
        binding.dotsIndicator.setViewPager2(binding.viewPagerImages)

        // Загружаем изображения
        loadImagesToFiles(car.imageUrls)

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
                    binding.textOwnerName.text = "${userData.Surname}, ${userData.Name}, ${userData.Middle_name}"
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

        // Характеристики
        binding.textBrandModel.text = "${car.Марка} ${car.Модель}"
        binding.textYear.text = "${car.Год_выпуска}"
        binding.textTransmission.text = "${car.Коробка_передач}"
        binding.textPrice.text = "${car.Цена_за_сутки}"
        binding.textLocation.text = "${car.Местоположение}"
        binding.textBodyType.text = "${car.Тип_кузова}"
        binding.textAvailability.text = if (car.Доступность == true) "Доступно" else "Недоступно"
        binding.textDescription.text = car.Описание ?: "Описание отсутствует"
    }

    private fun loadImagesToFiles(imageUrls: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val files = mutableListOf<File>()

            imageUrls.forEachIndexed { index, url ->
                try {
                    val file = File.createTempFile("image_$index", ".jpg", cacheDir)
                    val inputStream = URL(url).openStream()
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    inputStream.close()
                    outputStream.close()
                    files.add(file)
                    Log.d("CarDetailActivity", "Скачан файл: ${file.absoluteFile}")
                } catch (e: Exception) {
                    Log.e("CarDetailActivity", "Ошибка загрузки изображения $url: ${e.message}")
                }
            }
            withContext(Dispatchers.Main) {
                files.forEach { imageAdapter.addFile(it) }
                if (files.isEmpty()) {
                    Log.d("CarDetailActivity", "Нет изображений для отображения")
                }
                imageAdapter.notifyDataSetChanged()
            }
        }
    }
}