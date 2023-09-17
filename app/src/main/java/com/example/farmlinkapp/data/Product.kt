package com.example.farmlinkapp.data

import android.os.Parcelable
import com.example.farmlinkapp.util.Resource
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val seller: User = User(),
    val productType: String = "",
    val productLocation: String = "",
    val price: Double = 0.0,
    val description: String? = null,
    val quantity: Double = 0.0,
    val images: List<String> = emptyList()
) : Parcelable