package de.tillhub.paymentengine


class CardSaleConfig {

    var applicationName: String = DEFAULT_APPLICATION_NAME
    var operatorId: String = DEFAULT_OPERATOR_ID
    var saleId: String = DEFAULT_SALE_ID // Unique cash register ID
    var pin: String = DEFAULT_PIN // Terminal pin
    var poiId: String = DEFAULT_POI_ID // Unique terminal ID
    var poiSerialNumber: String = DEFAULT_POI_SERIAL // Unique terminal serial
    var isoCurrencyNumber: String = DEFAULT_CURRENCY_CODE
    var trainingMode: Boolean = DEFAULT_TRAINING_MODE

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
