package com.example.demo123

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Register : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private var currentUserId: Int = -1 // Для хранения ID текущего пользователя

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val TextEmail: EditText
        val TextPassword: EditText
        val buttonReg: Button

        TextEmail = findViewById(R.id.TextEmail)   //присвоение элементов страницы переменным
        TextPassword = findViewById(R.id.TextPassword)
        buttonReg = findViewById(R.id.buttonReg)


        buttonReg.setOnClickListener {
            getClient()
            val Esmail = TextEmail.text.toString()
            val Password = TextPassword.text.toString()
            val isSuccess: Boolean

            if (Esmail.isEmpty() || Password.isEmpty()){
                Toast.makeText(this@Register, "Пожалуйста введите данные в поля", Toast.LENGTH_SHORT).show()   //валидация данных
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = getClient().auth.signUpWith(Email){   //метод getClient из библиотеки Supabase auth
                        email = Esmail
                        password = Password
                    }
                    runOnUiThread{
                        // Успешная регистрация, так как мы попали сюда без исключения
                        Toast.makeText(this@Register, "Регистрация прошла успешно! Проверьте вашу почту для подтверждения.",Toast.LENGTH_SHORT).show()
                        Log.d("Registration", "Успешная регистрация: ${result}") // Лог для отладки
                    }
                }
                catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@Register,
                            "Ошибка регистрации попробуйте ещё раз",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "RegistrationError",
                            "Ошибка регистрации: ${e.message}",
                            e
                        ) // Лог ошибки с сообщением об исключении
                    }
                }
            }
        }
    }





private fun getData(){
    lifecycleScope.launch {
        val client = getClient()
        val supabaseReponse = client.postgrest["User"].select()
        val data = supabaseReponse.decodeList<User>()

        if (data.isNotEmpty()){
        val firstUser = data.first()
            currentUserId = firstUser.id // Сохраняем ID текущего пользователя

            editTextEmail.setText(firstUser.Email.toString())
            editTextPassword.setText(firstUser.Password.toString())

            Toast.makeText(this@Register, "Данные успешно загружены", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this@Register, "Произошла ошибка", Toast.LENGTH_SHORT).show()
        }
    }

    }



    /*@OptIn(ExperimentalContracts::class)
    private fun saveData() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            lifecycleScope.launch {
                val client = getClient()
                val updateResult = client.postgrest["User"]
                    .update(
                        {
                            User::Email setTo email
                            User::Password setTo password
                        }
                    )
                    .isEqualTo(User::id, currentUserId) // *Важно!* Обновляем запись по ID текущего пользователя
                    .execute() // Вызываем execute() и получаем Result

                updateResult.onSuccess { _ -> // Используем onSuccess, результат обновления нам не важен (можно использовать `_`)
                    Toast.makeText(this@Register, "Данные успешно обновлены", Toast.LENGTH_SHORT).show()
                }.onFailure { exception -> // Используем onFailure для обработки ошибок
                    if (exception is HttpRequestException) {
                        val errorMessage = "Ошибка обновления: HTTP ${exception.status} - ${exception.message}"
                        Toast.makeText(this@Register, errorMessage, Toast.LENGTH_LONG).show()
                        println("HTTP Error: $errorMessage")
                    } else {
                        val errorMessage = "Ошибка обновления: ${exception.message ?: "Неизвестная ошибка"}"
                        Toast.makeText(this@Register, errorMessage, Toast.LENGTH_LONG).show()
                        println("Error: $errorMessage")
                    }
                    exception.printStackTrace()
                }
            }
        } else {
            Toast.makeText(this@Register, "Заполните все поля", Toast.LENGTH_SHORT).show()

        }
    }*/



}





    internal fun getClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://twkqrtcvsuwoyrbleuiu.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR3a3FydGN2c3V3b3lyYmxldWl1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDAwNjc0MDYsImV4cCI6MjA1NTY0MzQwNn0.7v4fKtjArOa5PQSFakKtlQ7YI5UJb4Ju5Mf9rmnu8ew"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }


