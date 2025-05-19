package com.example.demo123
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val Surname: String,
    val Name: String,
    val Middle_name: String,
    val Passport: Long,
    val Number_VY: Long,
    val Phone: String
)