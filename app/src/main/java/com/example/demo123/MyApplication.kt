package com.example.demo123

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.demo123.data.customSerializersModule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    lateinit var supabase: SupabaseClient
        private set
    lateinit var authManager: AuthManager
        private set

    override fun onCreate() {
        super.onCreate()
        supabase = getClient()

        // Создаём MasterKey для шифрования
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)


        // Создаём EncryptedSharedPreferences и настраиваем для безопасного хранения токенов
        val sharedPreferences = EncryptedSharedPreferences.create(
            "PreferencesFilename",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Инициализируем AuthManager
        authManager = AuthManager(supabase.auth, sharedPreferences, supabase)

        //восстановление сессии при запуске
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        if (refreshToken != null){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    supabase.auth.refreshSession(refreshToken)
                }catch (e: Exception){
                    Log.e("MyApplication","Ошибка восстановления сессии: ${e.message}")
                }
            }
        }
    }
    private fun getClient(): SupabaseClient { // Теперь метод private, так как вызывается только внутри MyApplication
        return createSupabaseClient(
            supabaseUrl = "https://twkqrtcvsuwoyrbleuiu.supabase.co", // Твой URL
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR3a3FydGN2c3V3b3lyYmxldWl1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDAwNjc0MDYsImV4cCI6MjA1NTY0MzQwNn0.7v4fKtjArOa5PQSFakKtlQ7YI5UJb4Ju5Mf9rmnu8ew" // Твой anon key
        ) {
            install(Postgrest)
            install(Auth) {
                autoLoadFromStorage = true //Автоматически загружать сессию из хранилища
                alwaysAutoRefresh = true //Автоматически обновлять токен
            }
            install(Storage)
            httpEngine = CIO.create {
                requestTimeout = 40000 // 30 секунд
                maxConnectionsCount = 40000 // 30 секунд
            }

        }
    }
}