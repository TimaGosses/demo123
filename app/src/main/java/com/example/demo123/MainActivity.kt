package com.example.demo123

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.R.attr.password
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.demo123.LoginActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class MainActivity : AppCompatActivity() {

    private lateinit var editTextId: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var button1: Button
    private lateinit var supabaseClient: SupabaseClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        supabaseClient = (application as MyApplication).supabase

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val session = supabaseClient.auth.currentSessionOrNull()
                withContext(Dispatchers.Main) {
                    if (session != null){
                        startActivity(Intent(this@MainActivity, ListCar::class.java))
                        finish()
                    }
                    else{
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main) {
                    Log.e("MainActivity","Ошибка проверки сессии: ${e.message}")
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }

    }

}

@Serializable
data class User(
    var id: Int = 0,
    var Email: String = "",
    var Password: String = ""
)


