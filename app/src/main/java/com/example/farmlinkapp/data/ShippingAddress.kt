package com.example.farmlinkapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShippingAddress(
    val addressType: String,
    val fullNameShipping: String,
    val phone: String,
    val street: String,
    val barangay: String,
    val city: String,
    val province: String,
    val postalCode: String
): Parcelable {
    constructor(): this("","","","","","","", "")
}
