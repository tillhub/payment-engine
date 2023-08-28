package de.tillhub.paymentengine

import android.app.Activity
import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransportConfiguration
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal

interface CardPaymentManager {
    val transactionStateFlow: StateFlow<LavegoTerminalOperation>
    val transactionState: LavegoTerminalOperation

    fun connect(activity: Activity): PaymentTerminalConnection?

    fun disconnect(connection: PaymentTerminalConnection)

    /**
     * This method is called to start of a card payment transaction,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency)

    /**
     * This method is called to start of a card payment reversal,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startReversalTransaction(receiptNo: String)

    /**
     * This method is called to start of a partial card payment refund,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startPartialRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency)

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

    fun getTransportConfiguration(): TransportConfiguration
    fun getSaleConfiguration(): SaleConfiguration

    fun onStatus(status: String)
    fun onReceipt(receipt: String)
    fun onCompletion(completion: String)
    fun onError(error: String)
}