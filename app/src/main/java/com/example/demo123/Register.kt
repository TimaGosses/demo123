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
import kotlinx.coroutines.withContext


class Register : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonReg: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextEmail = findViewById(R.id.TextEmail)
        editTextPassword = findViewById(R.id.TextPassword)
        buttonReg = findViewById(R.id.buttonReg)

        buttonReg.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите данные в поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val supabaseClient = (application as MyApplication).supabase
                    // Регистрация и автоматический вход
                    supabaseClient.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    // Автоматический вход после регистрации
                    supabaseClient.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Register,
                            "Регистрация прошла успешно! Переход к заполнению профиля.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@Register, ProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Register,
                            "Ошибка регистрации: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("RegistrationError", "Ошибка: ${e.message}", e)
                    }
                }
            }
        }
    }
}