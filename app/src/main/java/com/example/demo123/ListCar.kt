package com.example.demo123

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo123.MyApplication
import com.example.demo123.R
import com.example.demo123.AuthManager
import com.example.demo123.data.CarRepository
import com.example.demo123.models.ListCarViewModel
import com.example.demo123.CarData
import com.example.demo123.databinding.ActivityListCarBinding
import com.example.demo123.models.ListCarViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.jvm.java


class ListCar : AppCompatActivity() {
    private lateinit var binding: ActivityListCarBinding
    private lateinit var carAdapter: CarAdapter
    private val viewModel: ListCarViewModel by viewModels {
        ListCarViewModelFactory(application as MyApplication, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка RecyclerView
        binding.carRecyclerView.layoutManager = LinearLayoutManager(this)
        carAdapter = CarAdapter { car ->
            Log.d("ListCar", "Клик по автомобилю, передаем данные: car_id = ${car.car_id}, Марка = ${car.Марка}")
            val intent = Intent(this@ListCar, CarDetailActivity::class.java)
            intent.putExtra("CarDetail", car)
            Log.d("ListCar", "Создан Intent с extra: CarDetail: $car")
            startActivity(intent)
        }
        binding.carRecyclerView.adapter = carAdapter

        // Отключение кнопки поиска до загрузки данных
        binding.buttonSearch.isEnabled = false

        // Обработчики кнопок
        binding.buttonGetCarList.setOnClickListener {
            startActivity(Intent(this, GetCar::class.java))
        }

        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.errorView.setOnClickListener {
            viewModel.checkAuthAndFetchData()
        }

        // Инициировать загрузку данных
        viewModel.checkAuthAndFetchData()

        // Наблюдение за данными
        lifecycleScope.launch {
            viewModel.searchResult.collect { cars ->
                carAdapter.submitList(cars)
                binding.buttonSearch.isEnabled = true
                if (cars.isEmpty() && binding.editTextSearch.text.toString().isNotEmpty()) {
                    Toast.makeText(this@ListCar, "Автомобили не найдены", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Наблюдение за ошибками
        lifecycleScope.launch {
            viewModel.errorFlow.collect { message ->
                if (message != null) {
                    Toast.makeText(this@ListCar, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Наблюдение за загрузкой
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Наблюдение за видимостью списка
        lifecycleScope.launch {
            viewModel.isListVisible.collect { isListVisible ->
                binding.carRecyclerView.visibility = if (isListVisible) View.VISIBLE else View.GONE
            }
        }

        // Наблюдение за видимостью ошибки
        lifecycleScope.launch {
            viewModel.isErrorVisible.collect { isVisible ->
                binding.errorView.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
        }

        // Обработка поиска
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            viewModel.updateSearchQuery(query)
        }
        //запуск загрузки данных
        viewModel.checkAuthAndFetchData()
    }
}