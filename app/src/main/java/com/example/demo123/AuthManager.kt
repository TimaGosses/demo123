package com.example.demo123

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.*
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow // Может потребоваться для Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import kotlin.jvm.java

data class Profile(val id: String, val name: String, val phone: String)
data class CarImage(val id: String, val user_id: String, val car_id: String, val image_url: String)

class AuthManager(
    private val authClient: Auth,
    private val sharedPreferences: SharedPreferences, //для хранения токенов
    private var SupabaseClient: SupabaseClient
) {
    //Автоматически запускаем подписку при создании экземпляра
    init {
        setupSessionListener()
    }
    //Подписка на обновление сессии
    private fun setupSessionListener(){
        authClient.sessionStatus.onEach { status ->
            if(status is SessionStatus.Authenticated){
                saveSession(status.session)
            }
        }.launchIn(CoroutineScope(Dispatchers.Main))

    }
    //Вход  в систему
        suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            // Выполняем вход с помощью Email-провайдера
             authClient.signInWith(Email) {  //Регистрация с помощью supabase Auth
                this.email = email    //вставка данных из полей в БД
                this.password = password
            }
            //получаем текущую сессию
            val session = authClient.currentSessionOrNull()
                ?: return Result.failure(Exception("Не удалось получить сессию после входа"))
            saveSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun restoreSession(): Result<Unit>{
        return try {
            val refreshToken = sharedPreferences.getString("refresh_token", null)
            if (refreshToken != null){
                authClient.refreshSession(refreshToken)
                val session = authClient.currentSessionOrNull()
                    ?: return Result.failure(Exception("Не удалось получить сессию после восстановления"))
                saveSession(session)
                Result.success(Unit)
            }else{
                Result.failure(Exception("Нет сохраненной сессии"))
            }
        }catch (e: Exception){
            Result.failure(e)
        }
    }
    //Выход из системы
    suspend fun logout(): Result<Unit>{
        return try {
            authClient.signOut()
            //Очищаем сохраненные токены
            with(sharedPreferences.edit()){
                clear()
                apply()
            }
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
    //Получение текущего пользователя
    suspend fun getCurrentUser(): String?{
        return authClient.currentUserOrNull()?.id
    }
    //Сохранение сессии
    suspend fun saveSession(session: io.github.jan.supabase.auth.user.UserSession){
        with(sharedPreferences.edit()){
            putString("access_token", session.accessToken)
            putString("refresh_token", session.refreshToken)
            putString("user_id", session.user?.id)
            apply()
        }
    }
    suspend fun uploadCarImages(context: Context,carId: String, imageUris: List<Uri>): Result<Unit> {
        return try {
            val userId = authClient.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Пользователь не авторизован"))

            imageUris.forEach { uri ->
                val fileName = "${UUID.randomUUID()}.jpg"
                val path = "user_$userId/car_$carId/$fileName"

                // Загрузка изображения в Storage
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Не удалось открыть изображение")
                SupabaseClient.storage.from("Изображение автомобиля")
                    .upload(path, inputStream.readBytes(), options = { // Явно указываем параметр options и передаем лямбду
                        upsert = false
                    })

                //Получение публичной ссылки
                val publicUrl =
                    SupabaseClient.storage.from("Изображение автомобиля").publicUrl(path)

                //Сохранение метаданных в таблицу Изображение автомобиля
                SupabaseClient.from("Изображение автомобиля").insert(
                    CarImage(
                        id = UUID.randomUUID().toString(),
                        user_id = userId,
                        car_id = carId,
                        image_url = publicUrl
                    )
                )
            }
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}
