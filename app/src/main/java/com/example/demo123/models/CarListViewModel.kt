package com.example.demo123.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import com.example.demo123.AuthManager
import com.example.demo123.CarListes
import com.example.demo123.CarLists
import com.example.demo123.data.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

class ListCarViewModel constructor(
    private val repository: CarRepository,
    private val authManager: AuthManager,
    private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<List<CarListes>>(emptyList())
    val searchResult: StateFlow<List<CarListes>> = _searchResult.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isListVisible = MutableStateFlow(true)
    val isListVisible: StateFlow<Boolean> = _isListVisible.asStateFlow()

    private val _isErrorVisible = MutableStateFlow(false)
    val isErrorVisible: StateFlow<Boolean> = _isErrorVisible.asStateFlow()

    private val _cars = MutableStateFlow<List<CarListes>>(emptyList())
    val cars: StateFlow<List<CarListes>> = _cars.asStateFlow()
    val allCars: Flow<List<CarListes>> = repository.getAllCars()

    init {
        viewModelScope.launch {
            repository.getAllCarsFlow().collect { cars ->
                _searchResult.value = cars.filter { car ->
                    val query = _searchQuery.value.lowercase()
                    query.isEmpty() ||
                            car.Марка.lowercase().contains(query) ||
                            car.Модель.lowercase().contains(query)
                }
                Log.d("CarListViewModel", "Фильтр машин: ${_searchResult.value.size}")
            }
        }
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
            _isListVisible.value = true
            _isErrorVisible.value = false
            repository.syncCars(context)
            _isListVisible.value = true
        } catch (e: Exception) {
            _errorFlow.value = "Ошибка загрузки данных: ${e.message}"
            val cachedCars = repository.getAllCarsFlow().firstOrNull() ?: emptyList()
            if (cachedCars.isNotEmpty()) {
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
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            _searchResult.value = emptyList()
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _searchResult.value = repository.searchCars(query)
        }
    }
}