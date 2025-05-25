package com.example.demo123.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo123.AuthManager
import com.example.demo123.CarLists
import com.example.demo123.data.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull

class ListCarViewModel(
    private val repository: CarRepository,
    private val authManager: AuthManager
) : ViewModel() {

    // Flow для всех автомобилей (без поиска)
    val cars: Flow<List<CarLists>> = repository.getCachedCars()

    // StateFlow для хранения поискового запроса
    private val searchQuery = MutableStateFlow("")

    // Flow для результатов поиска
    val searchResults: Flow<List<CarLists>> = searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            repository.getCachedCars()
        } else {
            repository.searchCars(query)
        }
    }

    // Flow для показа ошибок
    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    // Flow для управления видимостью прогресс-бара
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading

    // Flow для управления видимостью списка
    private val _isListVisible = MutableStateFlow(true)
    val isListVisible: Flow<Boolean> = _isListVisible

    // Flow для управления видимостью ошибки
    private val _isErrorVisible = MutableStateFlow(false)
    val isErrorVisible: Flow<Boolean> = _isErrorVisible

    init {
        // Проверяем авторизацию и загружаем данные при создании ViewModel
        checkAuthAndFetchData()
    }

    // Проверка авторизации и загрузка данных
    fun checkAuthAndFetchData(context: Context? = null) {
        viewModelScope.launch {
            if (authManager.checkAuth()) {
                fetchCarsFromSupabase()
            } else {
                _errorFlow.emit("Требуется авторизация")
                context?.let { authManager.redirectToLogin(it) }
            }
        }
    }

    // Загрузка данных из Supabase
    private suspend fun fetchCarsFromSupabase() {
        try {
            _isLoading.value = true
            _isListVisible.value = false
            _isErrorVisible.value = false
            repository.syncWithSupabase()
            _isListVisible.value = true
        } catch (e: Exception) {
            _errorFlow.emit("Ошибка загрузки данных: ${e.message}")
            val cachedCars = repository.getCachedCars().firstOrNull() ?: emptyList()
            if (cachedCars.isNotEmpty()) {
                _isListVisible.value = true
            } else {
                _isErrorVisible.value = true
            }
        } finally {
            _isLoading.value = false
        }
    }

    // Обновление поискового запроса
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    // Очистка кэша
    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
        }
    }
}