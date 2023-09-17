package com.example.farmlinkapp.helper

data class TableRow(
    val name: String,
    val quantity: Double
){
    constructor(): this("", 0.0)
}
