package com.example.demo123

import android.content.Intent
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputBinding
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo123.databinding.ActivityCarListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private lateinit var binding: ActivityCarListBinding
    private lateinit var carAdapter: CarAdapter
    private lateinit var repository: CarRepository

    //переменные для хранения текущих значений
    private var selectedBrand: String? = null
    private var selectedPriceRange: String? = null
    private var selectedBodyType: String? = null
    private var selectedCarClass: String? = null


class CarListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_car_list)

        //настройка Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //получаем MyApplication
        val app = (application as MyApplication)

        //Инициализация репозитория
        repository = CarRepository(app)

        //насторйка RecycleView
        binding.recyclerViewCars.layoutManager = LinearLayoutManager(this@CarListActivity)
        carAdapter = CarAdapter(emptyList())
        binding.recyclerViewCars.adapter = carAdapter

        //Загрузка данных без фильтров
        loadCars()

        //Настройка фильтров
        setupFilters()

        //Настройка нижней навигации
        binding.bottomNavigation.selectedItemId = R.id.nav_search
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> true
                R.id.nav_rentals -> {
                    startActivity(Intent(this@CarListActivity, RentalsActivity::class.java))
                    true
                }

                R.id.nav_my_cars -> {
                    startActivity(Intent(this@CarListActivity, MyCarsActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this@CarListActivity, ProfActivity::class.java))
                    true
                }
            }else -> false
        }
    }

//подключение меню для Toolber
    override fun onCreateOptionsMeny(menu: Menu?): Boolean{
      menuInlater.inflate(R.menu.toolbar_menu, menu)
      return true
}
//Обработка нажатия на элементы меню
override fun onOptionsItemSelected(item: MenuItem): Boolean{
    return when (item.itemId){
        android.R.id.home ->{       //обработка нажатия на кнопку назад
            finish()
            true
        }
        R.id.action_refresh -> {        //обработка нажатия на кнопку обновить
            selectedBrand = null
            selectedPriceRange = null
            selectedBodyType = null
            selectedCarClass = null
            binding.spinnerBrand.setSelection(0)
            binding.spinnerClass.setSelection(0)
            binding.spinnerPrice.setSelection(0)
            binding.spinnerBodyType.setSelection(0)
            loadCars()
            Toast.makeText(this,"Список обновлен",Toast.LENGTH_SHORT).show()
            true
        }else -> super.onOptionsItemSelected(item)
    }
}
private fun setupFilters(){
    //Фильтр по марке
    binding.spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long){
            val brand = parent.getItemAtPosition(position).toString()
            selectedBrand = if (brand == "Марка") null else brand
            loadCars()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            selectedBrand = null
            loadCars()
        }
    }
    //Фильтр по кузову
    binding.spinnerBodyType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val bodyType = parent.getItemAtPosition(position).toString()
            selectedBodyType = if (bodyType = "Тип_кузова") null else bodyType
            loadCars()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            selectedBodyType = null
            loadCars()
        }

    }
    //Фильтр по стоимости
    binding.spinnerPrice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val price = parent.getItemAtPosition(position).toString()
            selectedPriceRange = if (price = "Цена_за_сутки") null else price
            loadCars()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            selectedPriceRange = null
            loadCars()
        }
    }
    // Фильтр по классу авто
    binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
            val carClass = parent.getItemAtPosition(position).toString()
            selectedCarClass = if (carClass == "Класс авто") null else carClass
            loadCars()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            selectedCarClass = null
            loadCars()
        }
    }
}

private fun loadCars() {
    val (priceMin, priceMax) = when (selectedPriceRange) {
        "До 2000 ₽" -> Pair(null, 2000)
        "2000-4000 ₽" -> Pair(2000, 4000)
        "Более 4000 ₽" -> Pair(4000, null)
        else -> Pair(null, null)
    }

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val cars = repository.getCars(
                brand = selectedBrand,
                bodyType = selectedBodyType,
                priceMin = priceMin,
                priceMax = priceMax,
                carClass = selectedCarClass
            )
            carAdapter = CarAdapter(cars)
            binding.recyclerViewCars.adapter = carAdapter
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@CarListActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
        }
    }
}