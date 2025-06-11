package com.example.demo123.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demo123.MyApplication
import com.example.demo123.data.CarDao
import com.example.demo123.data.CarRepository
import com.example.demo123.data.DatabaseProvider
import io.github.jan.supabase.SupabaseClient

class ListCarViewModelFactory(
    private val application: MyApplication,
    private val context: Context, // Добавляем context
    //private val supabaseClient: SupabaseClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListCarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val carDao: CarDao = DatabaseProvider.getDatabase(application).carDao()
            return ListCarViewModel(
                repository = CarRepository(
                    supabaseClient = application.supabase,  //передача Supabase
                    carDao = carDao //передача CarDao
                ),
                authManager = application.authManager,
                context = context // Передаём context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}