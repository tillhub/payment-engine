package de.tillhub.paymentengine

import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransportConfiguration
import de.lavego.zvt.api.Apdu
import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.CardSaleConfig

interface PaymentManager {
    fun getTransportConfiguration(cardPaymentConfig: CardPaymentConfig): TransportConfiguration
    fun getSaleConfiguration(cardSaleConfig: CardSaleConfig): SaleConfiguration
    fun getSetupProtocol(cardPaymentConfig: CardPaymentConfig, cardSaleConfig: CardSaleConfig): SetupProtocol
}

sealed class SetupProtocol {
    object Nexo : SetupProtocol()
    data class ZVT(val register: Apdu) : SetupProtocol()
}