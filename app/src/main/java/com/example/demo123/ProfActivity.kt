package com.example.demo123

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo123.data.CarEntity
import com.example.demo123.databinding.ActivityProfBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfActivity : AppCompatActivity() {


    private lateinit var binding: ActivityProfBinding
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var carAdapter: CarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prof)
        binding = ActivityProfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //инициализация supabase
        supabaseClient = (application as MyApplication).supabase

        //настройка перехода к машине
        carAdapter = CarAdapter {car ->
            Log.d("ProfActivity","Настройка клика по автомобилю передаем данные car_id = ${car.car_id} Марка = ${car.Марка}")
            val intent = Intent(this@ProfActivity, CarDetailActivity::class.java)
            intent.putExtra("CarDetails",car)
            Log.d("ProfActivity","Создан PutExtra: ${car}")
            startActivity(intent)
        }
        binding.calListStore.layoutManager = LinearLayoutManager(this@ProfActivity)
        binding.calListStore.adapter = carAdapter

        //кнопка выхода
        binding.buttonOut.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    supabaseClient.auth.signOut()
                    withContext(Dispatchers.Main){
                        startActivity(Intent(this@ProfActivity, LoginActivity::class.java))
                        finish()
                    }
                }catch (e: Exception){
                    Log.e("ProfActivity","Ошибка выхода ${e.message}")
                }
            }
        }

        binding.calListStore.adapter = carAdapter

        binding.buttonback.setOnClickListener {
            val intent = Intent(this@ProfActivity, ListCar::class.java)
            startActivity(intent)
            finish()
        }

        loadProfileData()

    }
    private fun loadProfileData(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //получение данных пользователя
                val user = supabaseClient.auth.retrieveUser("JWT")
                val userId = user.id


                withContext(Dispatchers.Main) {
                    val userData = supabaseClient
                        .from("Пользователь")
                        .select{
                            filter { eq("user_id", userId) }
                        }
                        .decodeSingle<UserData>()
                    withContext(Dispatchers.Main) {
                        binding.textName.text = "${userData.Name} ${userData.Middle_name}"
                        binding.textEmails.text = user.email ?: "Нет Email"
                    }
                }

                //получение списка машин пользователя
                val cars = supabaseClient.from("Машина")
                    .select{
                        filter { eq("Владелец", userId) }
                    }
                    .decodeList<CarEntity>()

                val carListes = cars.map { carEntity ->
                    CarListes(
                        car_id = carEntity.car_id,
                        Марка = carEntity.Марка,
                        Модель = carEntity.Модель,
                        Год_выпуска = carEntity.Год_выпуска,
                        Коробка_передач = carEntity.Коробка_передач,
                        Тип_кузова = carEntity.Тип_кузова,
                        Местоположение = carEntity.Местоположение,
                        Доступность = carEntity.Доступность,
                        Название_типа_кузова = carEntity.Название_типа_кузова,
                        Название_города = carEntity.Название_города,
                        Название_коробки_передач = carEntity.Название_коробки_передач,
                        Владелец = carEntity.Владелец,
                        Цена_за_сутки = carEntity.Цена_за_сутки,
                        Описание = carEntity.Описание,
                        updated_at = carEntity.updated_at,
                        imageUrls = carEntity.imageUrls
                    )
                }

                withContext(Dispatchers.Main) {
                    carAdapter.submitList(carListes)
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main) {
                    //обработка ошибок
                    binding.textName.text = "Ошибка загрузки"
                    Toast.makeText(this@ProfActivity,"Ошибка загрузки данных профиля", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}