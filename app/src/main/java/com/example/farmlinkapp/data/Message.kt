package com.example.farmlinkapp.data


data class Message(
    val fromId: String,
    val text: String,
    val toId: String,
    val timestamp: Long
) {
    constructor(): this("", "", "", 0L)
}