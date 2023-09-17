package com.example.farmlinkapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SellerInformation(
    val shopName: String,
    val farmCertificate: String = "",
    val shopLocation: String = "",
    val paymentDetails: PaymentClientDetails
): Parcelable {
    constructor(): this("","", "", PaymentClientDetails())
}
