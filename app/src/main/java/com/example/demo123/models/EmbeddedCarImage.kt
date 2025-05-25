package com.example.demo123.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//Вспомогательная модель для изображений
@Serializable
data class EmbeddedCarImage(
    @SerialName("image_url") val image_url: String
)