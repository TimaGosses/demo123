package com.example.demo123

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


class ProfileActivity : AppCompatActivity() {

    private lateinit var editTextFamily: EditText  //обьявление пустых переменных
    private lateinit var editTextName: EditText
    private lateinit var editTextOtch: EditText
    private lateinit var editTextPassport: EditText
    private lateinit var editTextVY: EditText
    private lateinit var buttonGet: Button
    private lateinit var buttonGetC: Button //Добавление автомобиля

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editTextFamily = findViewById(R.id.editTextFamily)  //привязка переменной и объекта на странице
        editTextName = findViewById(R.id.editTextName)
        editTextOtch = findViewById(R.id.editTextOtch)
        editTextPassport = findViewById(R.id.editTextPassport)
        editTextVY = findViewById(R.id.editTextVY)
        buttonGet = findViewById(R.id.buttonGet)
        buttonGetC = findViewById(R.id.buttonGetC)
        val getData = buttonGet


        getData.setOnClickListener {  //функция нажатия кнопки
            newData()
        }
        buttonGetC.setOnClickListener { //функция нажатия кнопки
            val intent = Intent(this@ProfileActivity, GetCar::class.java)
            startActivity(intent)
        }
    }
    fun newData(){
        val Family = editTextFamily.text.toString().trim()   //вставка данных с полей в переменные
        val Name = editTextName.text.toString().trim()
        val Otch = editTextOtch.text.toString().trim()
        val PassportS = editTextPassport.text.toString().trim()
        val VYS = editTextVY.text.toString().trim()

        Log.e("ProfileActivity", "PassportS: '$PassportS', VYS: '$VYS' ")

            if (Family.isNotEmpty() && Name.isNotEmpty() && Otch.isNotEmpty() && PassportS.isNotEmpty() && VYS.isNotEmpty()){  //валидация данных с полей
                val Passport = PassportS.toLongOrNull()
                val VY = VYS.toLongOrNull()
                if (Passport != null && VY != null) { // Проверяем, что преобразование прошло успешно (не null)
                    saveSupabase(Family, Name, Otch, Passport, VY)
                } else {
                    Toast.makeText(this@ProfileActivity, "Поля Паспорт и В/У должны быть числами", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            else{
                Toast.makeText(this@ProfileActivity, "Пожалуйста заполните поля", Toast.LENGTH_SHORT).show()
                return
            }
            }
    fun saveSupabase(Family: String, Name: String, Otch: String, Passport: Long, VY: Long){  //функция сохранения данных в БД
        val supabaseClient = (application as MyApplication).supabase  //подключение к supabase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userData = UserData(   //вызов data class UserData и вставка в него значений из переменных
                    Surname = Family,
                    Name = Name,
                    Middle_name = Otch,
                    Passport = Passport,
                    Number_VY = VY
                )
                supabaseClient.from("Пользователь").insert(userData)  //сохранение данных в БД
                withContext(Dispatchers.Main){
                    Toast.makeText(this@ProfileActivity,"Данные сохранены", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Log.e("ProfileActivity", "Ошибка сохранения: ${e.message}", e)
                    Toast.makeText(this@ProfileActivity,"Произошла ошибка, данные не сохранены ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}