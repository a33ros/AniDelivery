package com.example.farmlinkapp.data

data class DirectCheckOutProduct(
    val checkOutProduct: Product,
    val checkOutPrice: Float,
    val quantityKg: Float
) {
    constructor(): this(Product(), 0f, 0f)
}
