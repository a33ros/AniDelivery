package com.example.farmlinkapp.data.order

import android.os.Parcelable
import com.example.farmlinkapp.data.CartProduct
import com.example.farmlinkapp.data.ShippingAddress
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random.Default.nextLong

@Parcelize
data class Order(
    val orderStatus: String = "",
    val totalPrice: Float = 0f,
    val products: List<CartProduct> = emptyList(),
    val address: ShippingAddress = ShippingAddress(),
    val date: String = SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0, 100_000_000_000) + totalPrice.toLong()
): Parcelable