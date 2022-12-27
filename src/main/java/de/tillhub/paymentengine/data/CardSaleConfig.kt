package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardSaleConfig(
    val applicationName: String = DEFAULT_APPLICATION_NAME,
    val operatorId: String = DEFAULT_OPERATOR_ID,
    val saleId: String = DEFAULT_SALE_ID, // Unique cash register ID
    val pin: String = DEFAULT_PIN, // Terminal pin
    val poiId: String = DEFAULT_POI_ID, // Unique terminal ID
    val poiSerialNumber: String = DEFAULT_POI_SERIAL, // Unique terminal serial
    val isoCurrencyNumber: String = DEFAULT_CURRENCY_CODE,
    val trainingMode: Boolean = DEFAULT_TRAINING_MODE
) : Parcelable {
    companion object {
        private const val DEFAULT_APPLICATION_NAME = "Tillhub GO"
        private const val DEFAULT_OPERATOR_ID = "ah"
        private const val DEFAULT_CURRENCY_CODE = "0978"
        private const val DEFAULT_TRAINING_MODE = true
        const val DEFAULT_SALE_ID = "registerProvider" // Unique cash register ID
        const val DEFAULT_PIN = "333333" // CCV Fly 2222
        const val DEFAULT_POI_ID = "66000001" // Unique terminal ID
        const val DEFAULT_POI_SERIAL = "" // Unique terminal serial
    }
}
