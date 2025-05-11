package com.example.demo123

import android.util.Log
import android.widget.Filter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class CarListViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {
    private val _cars = MutableLiveData<List<Car>>()
    val cars: LiveData<List<Car>> get() = _cars

    fun fetchCars(
        brandFilter: String? = null,
        priceSort: String? = null,
        bodyTypeFilter: String? = null
    ) {
        viewModelScope.launch {
            try {
                val response = supabaseClient.from("Машина").select {
                    if (brandFilter != null && brandFilter != "All"){
                        filter { eq("Марка", brandFilter) }
                    }
                    if (bodyTypeFilter != null && bodyTypeFilter != "All"){
                        filter { eq("Тип_кузова",bodyTypeFilter) }
                    }
                    when(priceSort){
                        "asc" -> order("price_per_day", Order.ASCENDING)
                        "desc" -> order("price_per_day", Order.DESCENDING)
                    }
                }.decodeList<Car>()
                _cars.postValue(response)
            }catch (e: Exception){
                _cars.postValue(emptyList())
                Log.e("CarListModel","Ошибка сортировки машин: ${e.message}")
            }
        }
    }

}
class CarListViewModelFactory(
    private val supabaseClient: SupabaseClient
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarListViewModel::class.java)){
            return CarListViewModel(supabaseClient) as T
        }
        throw IllegalArgumentException("Неизвестная ViewModel class")
    }
}