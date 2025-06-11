package com.example.demo123

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demo123.GetCar
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var textEmail: EditText  //Объявление пустых переменных
    private lateinit var textPassword: EditText
    private lateinit var buttonAuth: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var authManager: AuthManager
    private lateinit var buttonReg: Button
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)



        textEmail = findViewById(R.id.TextEmail)
        textPassword = findViewById(R.id.TextPassword)
        buttonAuth = findViewById(R.id.buttonAuth)
        buttonReg = findViewById(R.id.buttonReg)


        val supabaseClient = (application as MyApplication).supabase
        authManager = (application as MyApplication).authManager  //вызов authManager из Supabase Auth

        buttonAuth.setOnClickListener {  //функция нажатия на кнопку
            getLog()
        }
        buttonReg.setOnClickListener {
            var intent = Intent(this@LoginActivity, Register::class.java)  //функция перехода на другую страницу
            startActivity(intent)
        }

    }

    private fun getLog() {
        val email = textEmail.text.toString().trim()  //вставка данных из полей в переменные
        val password = textPassword.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {  //валидация данных
            Toast.makeText(this, "Пожалуйста заполните поля", Toast.LENGTH_SHORT).show()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Неверный формат Email", Toast.LENGTH_SHORT).show()
            return
        }

        //progressBar.visibility = View.VISIBLE
        //textView.visibility = View.GONE

        performLogin(email, password)
    }

    private fun performLogin(email: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                authManager.login(email, password)  //вызов authManager и вставка в него данных
            }

            // progressBar.visibility = View.GONE
            // textView.visibility = View.VISIBLE

            if (result.isSuccess) {  // условие с успешной авторизацей
                Toast.makeText(this@LoginActivity, "Успешная авторизация", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, ListCar::class.java)
                startActivity(intent) //переход на другую страницу
                //finish()

            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Ошибка авторизации: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}