package de.tillhub.paymentengine

import android.app.Activity
import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransactionData
import de.lavego.sdk.TransportConfiguration
import de.lavego.zvt.ZvtResponseCallback
import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal

interface CardPaymentManager {

    companion object {
        const val EXTRA_CARD_PAYMENT_CONFIG = "card_payment_config"
        const val EXTRA_CARD_SALE_CONFIG = "card_sale_config"
        const val EXTRA_PAYMENT_AMOUNT = "card_payment_amount"
    }

    val transactionStateFlow: StateFlow<LavegoTerminalOperation>
    val transactionState: LavegoTerminalOperation
    val zvtConnectionStateFlow: StateFlow<Boolean>
    val zvtConnectionState: Boolean

    val isNexoLoggedIn: Boolean

    fun connect(activity: Activity): PaymentTerminalConnection?

    fun disconnect(connection: PaymentTerminalConnection)

    /**
     * This method is called to start of a card payment transaction,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startPaymentTransaction(amount: BigDecimal)

    /**
     * This method is called to start of a card payment reversal,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startReversalTransaction(receiptNo: String)

    /**
     * This method is called to start of a partial card payment refund,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startPartialRefundTransaction(amount: BigDecimal)

    /**
     * This method is called to start of a terminal reconciliation,
     * it sets up the manager so the data from the operation is collected correctly.
     */
    fun startReconciliation()

    /**
     * This method is called after a card payment transaction is finished successfully or unsuccessfully,
     * it clears the manager so no old data is present that could corrupt the next transaction.
     */
    fun clearTransaction()

    fun nexoLoggedIn()
    fun setNexoResponse(txData: TransactionData)

    fun getZvtResponseCallback(): ZvtResponseCallback
    fun getTransportConfiguration(cardPaymentConfig: CardPaymentConfig): TransportConfiguration
    fun getSaleConfiguration(cardSaleConfig: CardSaleConfig): SaleConfiguration
}

@Suppress("UnnecessaryAbstractClass")
abstract class PaymentTerminalConnection {
    internal abstract fun disconnect()
}
