package com.example.demo123.models

import com.example.demo123.AuthManager
import com.example.demo123.CarLists

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo123.data.CarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ListCarViewModel(
    private val repository: CarRepository,
    private val authManager: AuthManager,
    private val context: Context
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CarLists>>(emptyList())
    val searchResults: StateFlow<List<CarLists>> = _searchResults.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isListVisible = MutableStateFlow(true)
    val isListVisible: StateFlow<Boolean> = _isListVisible.asStateFlow()

    private val _isErrorVisible = MutableStateFlow(false)
    val isErrorVisible: StateFlow<Boolean> = _isErrorVisible.asStateFlow()

    init {
        checkAuthAndFetchData()
    }

    fun checkAuthAndFetchData() {
        viewModelScope.launch {
            if (authManager.checkAuth()) {
                fetchCarsFromSupabase()
            } else {
                _errorFlow.value = "Требуется авторизация"
                authManager.redirectToLogin(context)
                _isErrorVisible.value = true
            }
        }
    }

    private suspend fun fetchCarsFromSupabase() {
        try {
            _isLoading.value = true
            _isListVisible.value = false
            _isErrorVisible.value = false
            repository.syncCars(context)
            val cars = repository.getAllCarsRaw()
            _searchResults.value = cars.map { carEntity ->
                CarLists(
                    car_id = carEntity.car_id,
                    Марка = carEntity.Марка,
                    Модель = carEntity.Модель,
                    Коробка_передач = carEntity.Коробка_передач,
                    Владелец = carEntity.Владелец,
                    Цена_за_сутки = carEntity.Цена_за_сутки,
                    Описание = carEntity.Описание,
                    updated_at = carEntity.updated_at,
                    Местоположение = carEntity.Местоположение,
                    Тип_кузова = carEntity.Тип_кузова,
                    Год_выпуска = carEntity.Год_выпуска,
                    Доступность = carEntity.Доступность ?: false,
                    imageUrls = carEntity.imageUrls
                )
            }.filter { car ->
                car.Марка?.contains(_searchQuery.value, ignoreCase = true) ?: false ||
                        car.Модель?.contains(_searchQuery.value, ignoreCase = true) ?: false
            }
            Log.d("ListCarViewModel", "Filtered cars: ${_searchResults.value.size}")
            _isListVisible.value = true
        } catch (e: Exception) {
            _errorFlow.value = "Ошибка загрузки данных: ${e.message}"
            val cachedCars = repository.getAllCarsRaw()
            if (cachedCars.isNotEmpty()) {
                _searchResults.value = cachedCars.map { carEntity ->
                    CarLists(
                        car_id = carEntity.car_id,
                        Марка = carEntity.Марка,
                        Модель = carEntity.Модель,
                        Коробка_передач = carEntity.Коробка_передач,
                        Владелец = carEntity.Владелец,
                        Цена_за_сутки = carEntity.Цена_за_сутки,
                        Описание = carEntity.Описание,
                        updated_at = carEntity.updated_at,
                        Местоположение = carEntity.Местоположение,
                        Тип_кузова = carEntity.Тип_кузова,
                        Год_выпуска = carEntity.Год_выпуска,
                        Доступность = carEntity.Доступность ?: false,
                        imageUrls = carEntity.imageUrls
                    )
                }.filter { car ->
                    car.Марка?.contains(_searchQuery.value, ignoreCase = true) ?: false ||
                            car.Модель?.contains(_searchQuery.value, ignoreCase = true) ?: false
                }
                _isListVisible.value = true
            } else {
                _isErrorVisible.value = true
            }
        } finally {
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val cars = repository.getAllCarsRaw()
            _searchResults.value = cars.map { carEntity ->
                CarLists(
                    car_id = carEntity.car_id,
                    Марка = carEntity.Марка,
                    Модель = carEntity.Модель,
                    Коробка_передач = carEntity.Коробка_передач,
                    Владелец = carEntity.Владелец,
                    Цена_за_сутки = carEntity.Цена_за_сутки,
                    Описание = carEntity.Описание,
                    updated_at = carEntity.updated_at,
                    Местоположение = carEntity.Местоположение,
                    Тип_кузова = carEntity.Тип_кузова,
                    Год_выпуска = carEntity.Год_выпуска,
                    Доступность = carEntity.Доступность ?: false,
                    imageUrls = carEntity.imageUrls
                )
            }.filter { car ->
                car.Марка?.contains(_searchQuery.value, ignoreCase = true) ?: false ||
                        car.Модель?.contains(_searchQuery.value, ignoreCase = true) ?: false
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
        }
    }
}