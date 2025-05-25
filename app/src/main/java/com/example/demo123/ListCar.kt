package com.example.demo123

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo123.MyApplication
import com.example.demo123.R
import com.example.demo123.AuthManager
import com.example.demo123.data.CarRepository
import com.example.demo123.CarData
import com.example.demo123.databinding.ActivityListCarBinding
import com.example.demo123.models.ListCarViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ListCar : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var carAdapter: CarAdapter
    private var carDataList = mutableListOf<CarLists>()
    private lateinit var binding: ActivityListCarBinding
    private lateinit var buttonGetCarList: Button
    private lateinit var buttonRegister: Button
    private lateinit var repository: CarRepository
    private lateinit var authManager: AuthManager
    private val viewModel: ListCarViewModel by viewModels {
        ListCarViewModelFactory(application as MyApplication)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация переменных
        recyclerView = binding.carRecyclerView
        buttonGetCarList = binding.buttonGetCarList
        buttonRegister = binding.buttonRegister
        repository = CarRepository(application as MyApplication)
        authManager = (application as MyApplication).authManager

        // Настройка RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        carAdapter = CarAdapter { car ->
            val intent = Intent(this, MyCarsActivity::class.java)
            intent.putExtra("car_id", car.car_id)
            startActivity(intent)
        }
        recyclerView.adapter = carAdapter

        // Отключаем кнопку поиска
        binding.buttonSearch.isEnabled = false

        // Обработчики кнопок
        buttonGetCarList.setOnClickListener {
            startActivity(Intent(this, GetCar::class.java))
        }
        buttonRegister.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Поиск
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            viewModel.updateSearchQuery(query)
        }

        // Повторная попытка загрузки при ошибке
        binding.errorView.setOnClickListener {
            viewModel.checkAuthAndFetchData()
        }

        viewModel.checkAuthAndFetchData()
        //подписка на данные и состояние UI
        lifecycleScope.launch {
            //подписка на результаты поиска
            viewModel.searchResults.collectLatest { cars ->
                carAdapter.submitList(cars)
                binding.buttonSearch.isEnabled = true
                if (cars.isEmpty() && binding.editTextSearch.text.toString().isNotEmpty()){
                    Toast.makeText(this@ListCar,"Автомобили не найдены",Toast.LENGTH_SHORT).show()
                }
            }
        }
        //подписка на ошибки
        lifecycleScope.launch {
            viewModel.errorFlow.collect { message ->
                Toast.makeText(this@ListCar, message, Toast.LENGTH_SHORT).show()
            }
        }

        //подписка на видимость прогресс-бара
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }


        //подписка на видимость списка
        lifecycleScope.launch {
            viewModel.isListVisible.collect { isVisible ->
                binding.carRecyclerView.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
        }
        //подписка на видимость ошибки
        lifecycleScope.launch {
            viewModel.isErrorVisible.collect { isVisible ->
                binding.errorView.visibility = if (isVisible) View.VISIBLE else View.GONE

            }
        }
    }
}
class ListCarViewModelFactory(private val application: MyApplication) :
        androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListCarViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ListCarViewModel(
                CarRepository(application),
                application.authManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}