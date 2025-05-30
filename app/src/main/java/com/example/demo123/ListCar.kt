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
    private lateinit var recyclerView: RecyclerView
    private lateinit var carAdapter: CarAdapter
    private var carDataList = mutableListOf<CarLists>()
    private lateinit var binding: ActivityListCarBinding
    private lateinit var buttonGetCarList: Button
    private lateinit var buttonRegister: Button
    private lateinit var repository: CarRepository
    private lateinit var authManager: AuthManager
    private val viewModel: ListCarViewModel by viewModels {
        ListCarViewModelFactory(application as MyApplication, this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListCarBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Инициализация переменных
        recyclerView = binding.carRecyclerView
        buttonGetCarList = binding.buttonGetCarList
        buttonRegister = binding.buttonRegister

        recyclerView.layoutManager = LinearLayoutManager(this)
        carAdapter = CarAdapter {car ->
            val intent = Intent(this@ListCar, MyCarsActivity::class.java)
            intent.putExtra("car_id",car.car_id)
            startActivity(intent)
        }
        recyclerView.adapter = carAdapter

        binding.buttonSearch.isEnabled = false

        buttonGetCarList.setOnClickListener {
            startActivity(Intent(this, GetCar::class.java))
        }

        buttonRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            viewModel.updateSearchQuery(query)
        }

        binding.errorView.setOnClickListener {
            viewModel.checkAuthAndFetchData()
        }
        viewModel.checkAuthAndFetchData()

        lifecycleScope.launch {
            viewModel.searchResult.collect{cars ->
                carAdapter.submitList(cars)
                binding.buttonSearch.isEnabled = true
                if (cars.isEmpty() && binding.editTextSearch.toString().isNotEmpty()) {
                    Toast.makeText(this@ListCar,"Автомобили не найдены", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorFlow.collect() {message ->
                if (message != null) {
                    Toast.makeText(this@ListCar,message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect{ isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isListVisible.collect{ isListVisiable ->
                binding.carRecyclerView.visibility = if (isListVisiable) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isErrorVisible.collect{ isVisiable ->
                binding.errorView.visibility = if (isVisiable) View.VISIBLE else View.GONE
            }
        }



    }

}
