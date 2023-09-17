package com.example.farmlinkapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val fullName: String,
    val email: String,
    val userId: String,
    var imagePath: String = ""
): Parcelable {
  constructor(): this("","","","")
}