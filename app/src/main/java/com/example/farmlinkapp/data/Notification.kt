package com.example.farmlinkapp.data

data class Notification(
    val notificationName: String,
    val notificationType: String,
    val alertDescription: String,
    val timestamp: String
) {
    constructor(): this("", "","", "")
}
