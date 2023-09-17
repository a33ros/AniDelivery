package com.example.farmlinkapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentClientDetails(
    val gCashNumber: String,
    val cardInformation: CardInformation
): Parcelable {
    constructor(): this("",CardInformation())
}
