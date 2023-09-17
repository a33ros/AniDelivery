package com.example.farmlinkapp.data.order

import android.os.Parcelable
import com.example.farmlinkapp.data.CartProduct
import com.example.farmlinkapp.data.Product
import com.example.farmlinkapp.data.ShippingAddress
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random.Default.nextLong

@Parcelize
data class DirectOrder(
    val orderStatus: String = "",
    val orderType: String = "",
    val deliveryType: String = "",
    val totalPrice: Double = 0.0,
    val buyQuantity: Double = 0.0,
    val product: Product = Product(),
    val address: ShippingAddress = ShippingAddress(),
    val date: String = "",
    val orderId: Long = nextLong(0, 100_000_000_000) + totalPrice.toLong()
): Parcelable