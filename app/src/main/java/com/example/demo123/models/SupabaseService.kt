package com.example.demo123.models

import com.example.demo123.CarLists
import com.example.demo123.MyApplication
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

object SupabaseService {

    suspend fun getCars(supabaseClient: SupabaseClient): List<CarLists>{
        return supabaseClient.from("Машина")
            .select()
            .decodeList<CarLists>()
    }
}