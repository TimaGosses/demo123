package com.example.demo123
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    @SerialName("user_id") val user_id: String,
    @SerialName("Surname") val Surname: String,
    @SerialName("Name") val Name: String,
    @SerialName("Middle_name") val Middle_name: String,
    @SerialName("Passport") val Passport: Long,
    @SerialName("Number_VY") val Number_VY: Long,
    @SerialName("Number_phone") val Number_phone: String
)