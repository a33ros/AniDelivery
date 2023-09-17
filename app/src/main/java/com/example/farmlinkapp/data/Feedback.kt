package com.example.farmlinkapp.data

data class Feedback(
    val userId: String,
    val rating: Float,
    val feedbackDescription: String
){
    constructor(): this("", 0f, "")
}
