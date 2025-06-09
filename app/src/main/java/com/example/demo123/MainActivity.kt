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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : AppCompatActivity() {

    private lateinit var editTextId: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var button1: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editTextId = findViewById(R.id.editTextTextId)
        editTextEmail = findViewById(R.id.editTextTextEmail)
        editTextPassword = findViewById(R.id.editTextTextPassword)
        button1 = findViewById(R.id.button1)
        getData()
        button1.setOnClickListener {
            val intent = Intent(this@MainActivity, Register::class.java)
            startActivity(intent)
        }
    }
    private fun getData(){
        lifecycleScope.launch {







        }
    }

    /*private fun getClient(): SupabaseClient{
        return createSupabaseClient(
            supabaseUrl = "https://twkqrtcvsuwoyrbleuiu.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR3a3FydGN2c3V3b3lyYmxldWl1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDAwNjc0MDYsImV4cCI6MjA1NTY0MzQwNn0.7v4fKtjArOa5PQSFakKtlQ7YI5UJb4Ju5Mf9rmnu8ew"
        ){
            install(Postgrest)
            install(Auth)
        }

    }*/

}

@Serializable
data class User(
    var id: Int = 0,
    var Email: String = "",
    var Password: String = ""
)


