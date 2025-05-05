package com.example.demo123

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo123.MyApplication
import com.example.demo123.CarRepository
import com.example.demo123.databinding.ActivityCarListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarListBinding
    private lateinit var carAdapter: CarAdapter
    private lateinit var repository: CarRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем MyApplication
        val app = application as MyApplication
        Log.d("CarListActivity","Подключение к Supabase")

        // Инициализируем репозиторий
        repository = CarRepository(app)

        // Настройка RecyclerView
        binding.recyclerViewCars.layoutManager = LinearLayoutManager(this)
        carAdapter = CarAdapter(emptyList())
        binding.recyclerViewCars.adapter = carAdapter

        // Загрузка всех автомобилей
        loadCars()
    }

    private fun loadCars() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("CarListActivity","Начало загрузки машин")
                val cars = repository.getCars() // Загружаем все автомобили без фильтров
                Log.d("CarListActivity","Загрузка: ${cars.size}, машины: $cars")
                carAdapter = CarAdapter(cars)
                binding.recyclerViewCars.adapter = carAdapter
                carAdapter.notifyDataSetChanged()  //Уведомление адаптера об изменении данных
                if (cars.isEmpty()){
                    Toast.makeText(this@CarListActivity,"Ошибка загрузки машин, мписое пуст", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CarListActivity","Ошибка: ${e.message}")
                Toast.makeText(this@CarListActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        }
    }
}