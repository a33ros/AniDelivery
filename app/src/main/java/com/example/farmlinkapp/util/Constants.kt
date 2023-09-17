package com.example.farmlinkapp.util

import com.google.android.gms.wallet.WalletConstants

object Constants {
    const val USER_COLLECTION = "user"

    const val PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST

    val SUPPORTED_NETWORKS = listOf(
        "MASTERCARD",
        "VISA")

    val SUPPORTED_METHODS = listOf(
        "PAN_ONLY")

    const val COUNTRY_CODE = "PH"

    const val CURRENCY_CODE = "PHP"

    val SHIPPING_SUPPORTED_COUNTRIES = listOf("PH")

    const val PAYMENT_GATEWAY_TOKENIZATION_NAME = "example"

    val PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS = mapOf(
        "gateway" to PAYMENT_GATEWAY_TOKENIZATION_NAME,
        "gatewayMerchantId" to "exampleGatewayMerchantId"
    )
}
