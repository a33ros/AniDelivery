package com.example.farmlinkapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardInformation(
    val holderName: String,
    val cardNumber: String,
    val cvv: String,
    val cardType: String
): Parcelable {
    constructor(): this("","","","")
}
