package com.example.farmlinkapp.data.order

sealed class OrderStatus(val status: String) {

    object Ordered: OrderStatus("Ordered")
    object Cancelled: OrderStatus("Cancelled")
    object Confirmed: OrderStatus("Confirmed")
    object Shipped: OrderStatus("Shipped")
    object Delivered: OrderStatus("Delivered")
    object Returned: OrderStatus("Returned")

}

fun getOrderStatus(status: String): OrderStatus {
    return when(status) {
        "Ordered" -> {
            OrderStatus.Ordered
        }
        "Cancelled" -> {
            OrderStatus.Cancelled
        }
        "Confirmed" -> {
            OrderStatus.Confirmed
        }
        "Shipped" -> {
            OrderStatus.Shipped
        }
        "Delivered" -> {
            OrderStatus.Delivered
        }
        else -> OrderStatus.Returned
    }
}
