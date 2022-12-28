package de.tillhub.paymentengine

import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransportConfiguration
import de.lavego.zvt.api.Apdu
import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTerminalOperation

interface TerminalManager {
    fun getTransportConfiguration(cardPaymentConfig: CardPaymentConfig): TransportConfiguration
    fun getSaleConfiguration(cardSaleConfig: CardSaleConfig): SaleConfiguration
    fun getSetupProtocol(cardPaymentConfig: CardPaymentConfig, cardSaleConfig: CardSaleConfig): SetupProtocol
    fun onStatus(status: String)
    fun onReceipt(receipt: String)
    suspend fun onCompletion(completion: String): LavegoTerminalOperation.Success
    suspend fun onError(error: String): LavegoTerminalOperation.Failed

    companion object {
        const val EXTRA_CARD_PAYMENT_CONFIG = "card_payment_config"
        const val EXTRA_CARD_SALE_CONFIG = "card_sale_config"
        const val EXTRA_PAYMENT_AMOUNT = "card_payment_amount"

        const val RESULT_DATA = "result_data"
    }
}

sealed class SetupProtocol {
    object Nexo : SetupProtocol()
    data class ZVT(val register: Apdu) : SetupProtocol()
}