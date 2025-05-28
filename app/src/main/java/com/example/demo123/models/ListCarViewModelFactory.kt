package com.example.demo123.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demo123.MyApplication
import com.example.demo123.data.CarRepository

class ListCarViewModelFactory(
    private val application: MyApplication,
    private val context: Context // Добавляем context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListCarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListCarViewModel(
                repository = CarRepository(application),
                authManager = application.authManager,
                context = context // Передаём context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}