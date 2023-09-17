package com.example.farmlinkapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartProduct(
    val product: Product,
    var quantity: Int
): Parcelable{
    constructor(): this(Product(), 1)
}
