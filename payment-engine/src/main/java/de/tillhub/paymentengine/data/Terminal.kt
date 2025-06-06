package de.tillhub.paymentengine.data

import android.os.Parcelable

interface Terminal : Parcelable {
    val id: String
    val saleConfig: CardSaleConfig
}