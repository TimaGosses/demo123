package com.example.demo123

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.SharedPreferences
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.*
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.Flow // Может потребоваться для Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val authManager = (application as MyApplication).authManager

        // Запускаем корутину для вызова suspend функции restoreSession
        CoroutineScope(Dispatchers.IO).launch {
            val result: Result<Unit> = authManager.restoreSession() // Укажите тип Result, если знаете

            withContext(Dispatchers.Main) {
                // Правильная проверка состояния Result
                when {
                    result.isSuccess -> {
                        // Если результат успешный (не содержит исключения)
                        // Вы можете получить значение, если оно есть: result.getOrNull()
                        startActivity(Intent(this@SplashActivity, ListCar::class.java))
                        finish()
                    }
                    result.isFailure -> {
                        // Если результат является ошибкой (содержит исключение)
                        // Вы можете получить исключение: result.exceptionOrNull()
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                    // Дополнительно можно добавить else для полной обработки всех случаев,
                    // хотя для Result.isSuccess и Result.isFailure это обычно не требуется,
                    // так как они покрывают все возможные состояния Result.
                }
            }
        }
    }
}