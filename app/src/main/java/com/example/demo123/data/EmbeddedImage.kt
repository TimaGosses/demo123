package com.example.demo123.data

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedImage(
    val car_id: String,
    val image_url: String
) {
}