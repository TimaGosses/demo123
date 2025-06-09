package com.example.demo123

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.touchlab.kermit.SimpleFormatter
import com.example.demo123.GetCar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.Serializable

import java.text.SimpleDateFormat
import java.util.UUID
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom.current


class ProfileActivity : AppCompatActivity() {

    private lateinit var editTextFamily: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextOtch: EditText
    private lateinit var editTextPassport: EditText
    private lateinit var editTextVY: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var buttonGet: Button
    private lateinit var buttonGetC: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        editTextFamily = findViewById(R.id.editTextFamily)
        editTextName = findViewById(R.id.editTextName)
        editTextOtch = findViewById(R.id.editTextOtch)
        editTextPassport = findViewById(R.id.editTextPassport)
        editTextVY = findViewById(R.id.editTextVY)
        editTextPhone = findViewById(R.id.editTextPhone)
        buttonGet = findViewById(R.id.buttonGet)
        buttonGetC = findViewById(R.id.buttonGetC)

        buttonGet.setOnClickListener {
            newData()
        }
        buttonGetC.setOnClickListener {
            val intent = Intent(this@ProfileActivity, GetCar::class.java)
            startActivity(intent)
        }
    }

    fun newData() {
        val Family = editTextFamily.text.toString().trim()
        val Name = editTextName.text.toString().trim()
        val Otch = editTextOtch.text.toString().trim()
        val PassportS = editTextPassport.text.toString().trim()
        val Phone = editTextPhone.text.toString().trim()
        val VYS = editTextVY.text.toString().trim()

        Log.d("ProfileActivity", "PassportS: '$PassportS', VYS: '$VYS'")

        if (Family.isNotEmpty() && Name.isNotEmpty() && Otch.isNotEmpty() && PassportS.isNotEmpty() && VYS.isNotEmpty()) {
            val Passport = PassportS.toLongOrNull()
            val VY = VYS.toLongOrNull()
            if (Passport != null && VY != null) {
                saveSupabase(Family, Name, Otch, Passport, VY, Phone)
            } else {
                Toast.makeText(this, "Поля Паспорт и В/У должны быть числами", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveSupabase(Family: String, Name: String, Otch: String, Passport: Long, VY: Long, Phone: String) {
        val supabaseClient = (application as MyApplication).supabase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получение user_id текущего аутентифицированного пользователя
                val user = supabaseClient.auth.currentUserOrNull()
                val userId = user?.id ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Проверка, существует ли пользователь в таблице
                val existingUser = supabaseClient.from("Пользователь")
                    .select {
                        filter { eq("user_id", userId) }
                    }.decodeList<UserData>().firstOrNull()

                if (existingUser != null) {
                    // Обновление существующей записи
                    val updatedUserData = UserData(
                        user_id = userId,
                        Surname = Family,
                        Name = Name,
                        Middle_name = Otch,
                        Passport = Passport,
                        Number_VY = VY,
                        Number_phone = Phone
                    )
                    Log.d("ProfileActivity", "Обновляемые данные: $updatedUserData")
                    supabaseClient.from("Пользователь")
                        .update(updatedUserData) {
                            filter { eq("user_id", userId) }
                        }
                } else {
                    // Создание новой записи
                    val newUserData = UserData(
                        user_id = userId,
                        Surname = Family,
                        Name = Name,
                        Middle_name = Otch,
                        Passport = Passport,
                        Number_VY = VY,
                        Number_phone = Phone

                    )
                    Log.d("ProfileActivity", "Сохраняемые данные: $newUserData")
                    supabaseClient.from("Пользователь").insert(newUserData)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Данные сохранены", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ProfileActivity, ListCar::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ProfileActivity", "Ошибка сохранения: ${e.message}", e)
                    Toast.makeText(
                        this@ProfileActivity,
                        "Произошла ошибка, данные не сохранены: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}