package com.example.demo123

import android.content.Intent
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.KeyPosition
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import co.touchlab.kermit.Message
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.supabaseJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.list

class GetCar : AppCompatActivity() {

    private lateinit var EditTextMark: EditText    //Обьявление переменных
    private lateinit var EditTextModel: EditText
    private lateinit var EditTextEarOfRelease: EditText
    private lateinit var EditTextPriceOfDay: EditText
    private lateinit var EditTextVIN: EditText
    private lateinit var EditTextDescription: EditText
    private lateinit var ButtonGetCar: Button
    private lateinit var spinnerKPP: Spinner
    private lateinit var spinnerReg: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var buttonGetImage: Button


    private var transmission: List<Transmission> = emptyList()   //Создание листов где в переменную transmission предается список объектов Transmission, и пока что он пустой
    private var city: List<City> = emptyList()
    private var region: List<Region> = emptyList()
    private var filteredLocations: List<City> = emptyList()
    private var CarId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) { //Метод вызывающийся при создании Activity
        super.onCreate(savedInstanceState) //Вызов метода onCreate
        enableEdgeToEdge()  //Настройка полноэкранного режима
        setContentView(R.layout.activity_get_car)   //установка макета
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        EditTextMark = findViewById(R.id.editTextMark)   //Связь переменных и объектов на странице
        EditTextModel = findViewById(R.id.editTextModel)
        EditTextEarOfRelease = findViewById(R.id.editTextEarOfRelease)
        EditTextPriceOfDay = findViewById(R.id.editTextPriceOfDay)
        EditTextVIN = findViewById(R.id.editTextVIN)
        EditTextDescription = findViewById(R.id.editTextDescription)
        ButtonGetCar = findViewById(R.id.buttonGetCar)
        spinnerKPP = findViewById(R.id.spinnerKPP)
        spinnerReg = findViewById(R.id.spinnerReg)
        spinnerCity = findViewById(R.id.spinnerCity)
        buttonGetImage = findViewById(R.id.buttonGetImage)

        val supabaseClient = (application as MyApplication).supabase   //Подключение к Supabase
        savedInstanceState?.let {
            val carId = it.getString("car_id")
            if (carId != null){
                buttonGetImage.isEnabled = true
            }
        }

        suspend fun <T : Any> loadDataIntosSpinner(  //Асинхронная функция(работает в фоновом режиме) для отображения и выбора данных для спиннеров
            tableName: String,  //Имя таблицы в БД
            spinner: Spinner,   // Выпадающий список
            decodeList: suspend () -> List<T>,  //функция которая загружает данные из базы данных и возвращает список обьектов T
            getName: (T) -> String,  //Функция извлекающая имя объекта T
            errorMessage: String,  //Сообщение об ошибке
            onDataLoaded: (List<T>) -> Unit //Callback для сохранения данных
        ){
            val supabaseClient = (application as MyApplication).supabase
            try {
                val data = decodeList()   //вызов функции decodeList, которую передавали в параметрах
                Log.d("GetCar","Загруженныу данные из $tableName: $data") //Логирование данных
                withContext(Dispatchers.Main){  //переключение выполнения кода на главный поток для работы с интерфейсом
                    val names = listOf("Выберите...") + data.map(getName)  //создание списка с одним элементом и со строкой "Выберите"
                    Log.d("GetCar","Имена для Spinner'a: $names")  //Логирование списка имен
                    val adapter = ArrayAdapter(this@GetCar, android.R.layout.simple_spinner_item, names)  //создаем адаптер который соеденяет данные с выпадающим списком
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)  //указываем как будет выглядеть выпадающий список когда его раскроют
                    spinner.adapter = adapter  //присваиваем адаптер к списку
                    onDataLoaded(data) //Сохраняем данные в нужную переменную
                }
            }catch (e: Exception){
                Log.e("GetCar","Ошибка загрузки данных из $tableName: $errorMessage", e) //Логирование ошибки
                withContext(Dispatchers.Main){
                    Toast.makeText(this@GetCar, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            loadDataIntosSpinner(
                tableName = "Коробка_передач_автомобиля",
                spinner = spinnerKPP,
                decodeList = {supabaseClient.from("Коробка_передач_автомобиля").select().decodeList<Transmission>()},
                getName = { (it as Transmission).Название},
                errorMessage = "Ошибка загрузки коробки передач",
                onDataLoaded = {transmission = it as List<Transmission>}

            )
            loadDataIntosSpinner(
                tableName = "Регион",
                spinner = spinnerReg,
                decodeList = {supabaseClient.from("Регион").select().decodeList<Region>()},
                getName = {(it as Region).Название},
                errorMessage = "Ошибка загрущки региона",
                onDataLoaded = {region = it as List<Region>}
            )
            loadDataIntosSpinner(
                tableName = "Город",
                spinner = spinnerCity,
                decodeList = {supabaseClient.from("Город").select().decodeList<City>()},
                getName = {(it as City).Название},
                errorMessage = "Ошибка загрузки города",
                onDataLoaded = {city = it as List<City>}
            )
        }

        ButtonGetCar.setOnClickListener {
            newCar()
        }
            buttonGetImage.setOnClickListener {
               /* val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
                    Toast.makeText(this@GetCar,"Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }*/
                val intent = Intent(this@GetCar, ImageCarActivity::class.java)
                //intent.putExtra("user_id", userId)
            startActivity(intent)
        }

    }





    fun newCar(){
        val Mark = EditTextMark.text.toString().trim()   //создание переменных и передача в них значений из тектовых полей
        val Model = EditTextModel.text.toString().trim()
        val EOR = EditTextEarOfRelease.text.toString().trim() //Год выпуска (Ear of release)
        val Transmission = spinnerKPP.selectedItemPosition
        val City = spinnerCity.selectedItemPosition
        val Region = spinnerReg.selectedItemPosition
        val POD = EditTextPriceOfDay.text.toString().trim() //Цена в день(Price of Day)
        val VIN = EditTextVIN.text.toString().trim()
        val EORS = EOR.toIntOrNull()
        val PODS = POD.toIntOrNull()
        val Description = EditTextDescription.text.toString().trim()
        val supabaseClient = (application as MyApplication).supabase //подключение к БД

        val user = supabaseClient.auth.currentUserOrNull() //Получаем Id текущего пользователя
        if (user == null){
            throw IllegalArgumentException("Пользователь не авторизован")
        }
        val ownerId = user.id //Id текущего пользователя типа String формата uuid

        Log.e("newCar","Id текущего пользователя: $ownerId")




        if(Mark.isBlank() || Model.isBlank() || EOR.isBlank() || Transmission == 0 || POD.isBlank() || City == 0 || Region == 0 || Description.isBlank()){ //Проверка заполнености полей
            Toast.makeText(this@GetCar, "Заполните поля!", Toast.LENGTH_SHORT).show()
            return
        }else if (EORS == null || PODS == null){
            Toast.makeText(this@GetCar, "Поля год выпуска и цена за день должны быть числами",
                Toast.LENGTH_SHORT).show()
            return
        }
        else if (VIN.length != 17 || !VIN.matches(Regex("^[A-HJ-NPR-Z0-9]{17}$", RegexOption.IGNORE_CASE))){  //Валидация поля VIN
            Toast.makeText(this@GetCar,"VIN должен быть длиной в 17 символов и не содержать буквы I,O,Q",
                Toast.LENGTH_SHORT).show()
            return
        }
        else {
            val transmissionId = transmission[Transmission - 1].id   // берем id коробки передач из списков
            val cityId = if (filteredLocations.isNotEmpty() && City > 0) {
                filteredLocations[City - 1].id
            } else if (city.isNotEmpty() && City > 0) {   //проверка наличия записей в бд
                city[City - 1].id
            } else {
                Toast.makeText(this@GetCar, "Выберите город", Toast.LENGTH_SHORT).show()
                return
            }
                val regionId = region[Region - 1].id
                Log.e("GetCar", "Отправляем данные VIN: $VIN, TransmossionID: $transmissionId, CityId: $cityId, RegionId: $regionId")

            lifecycleScope.launch {
                try {
                    //Проверка уникальности VIN
                    val existingCar = supabaseClient.from("Машина")
                        .select{
                            filter { eq("VIN", VIN) }
                        }
                        .decodeList<CarData>()
                    if (existingCar.isNotEmpty()){
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@GetCar,"АВтомомбиль с таким VIN уже существует", Toast.LENGTH_SHORT).show()
                            ButtonGetCar.isEnabled = true
                        }
                        return@launch
                    }
                    // Вызов saveCar
                    val carId = saveCar(Mark, Model, EORS, transmissionId, PODS, cityId, VIN, ownerId, Description)
                    withContext(Dispatchers.Main) {
                        if (carId != null) {
                            Toast.makeText(this@GetCar, "Автомобиль успешно добавлен", Toast.LENGTH_SHORT).show()
                            // Переход в ImageCarActivity с car_id
                            val intent = Intent(this@GetCar, ImageCarActivity::class.java).apply {
                                putExtra("car_id", carId)
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@GetCar, "Ошибка добавления автомобиля", Toast.LENGTH_SHORT).show()
                        }
                        ButtonGetCar.isEnabled = true

                    }

                }catch (e: Exception){
                    Log.e("GetCar", "Ошибка в newCar: ${e.message}",e)
                    Toast.makeText(this@GetCar,"Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    ButtonGetCar.isEnabled = true
                }
            }




        }

    }
    suspend fun saveCar(
        Mark: String,
        Model: String,
        EORS: Int,
        Transmission: Int,
        PODS: Int,
        CityId: Int,
        VIN: String,
        ownerId: String,
        Description: String
    ): String? {
        val supabaseClient = (application as MyApplication).supabase
        try {
            // Вставка местонахождения автомобиля
            val carLocation = CarLocation(Город = CityId)
            Log.d("GetCar", "Создаём запись в Местонахождение_автомобиля: $carLocation")
            supabaseClient.from("Местонахождение_автомобиля").insert(carLocation)
            Log.d("GetCar", "Запись в Местонахождение_автомобиля создана")

            // Получение последней записи местоположения
            val insertedLocation = supabaseClient.from("Местонахождение_автомобиля")
                .select {
                    filter { eq("Город", CityId) }
                    order("id", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(1)
                }
                .decodeSingle<CarLocation>()
            Log.d("GetCar", "Получена последняя запись: $insertedLocation")

            val locationId = insertedLocation.id
                ?: throw IllegalStateException("Не удалось получить id местоположения")

            // Создание данных автомобиля
            val carData = CarData(
                Марка = Mark,
                Модель = Model,
                Год_выпуска = EORS,
                Коробка_передач = Transmission,
                Цена_за_сутки = PODS,
                Местоположение = locationId.toInt(),
                VIN = VIN,
                Владелец = ownerId,
                Описание = Description
            )
            Log.d("GetCar", "Отправляем данные в Машина: $carData, Владелец: $ownerId, auth.uid: ${supabaseClient.auth.currentUserOrNull()?.id}")

            // Вставка в таблицу Машина и получение созданной записи
            val insertedCar = supabaseClient.from("Машина")
                .insert(carData) {
                    select()
                }
                .decodeSingle<CarDataWithId>()
            Log.d("GetCar", "Машина добавлена с car_id: ${insertedCar.car_id}")
            return insertedCar.car_id
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("GetCar", "Ошибка сохранения: ${e.message}", e)
                Toast.makeText(this@GetCar, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            }
            return null
        }
    }

    private fun logout() {
        val authManager = (application as MyApplication).authManager
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val result = authManager.logout()
                withContext(Dispatchers.Main) {
                    when {
                        result.isSuccess -> {
                            Toast.makeText(this@GetCar, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@GetCar, LoginActivity::class.java))
                            finish()
                        }
                        result.isFailure -> {
                            Toast.makeText(this@GetCar, "Ошибка выхода", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GetCar, "Ошибка ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}