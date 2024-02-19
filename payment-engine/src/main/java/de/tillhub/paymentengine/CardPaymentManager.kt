package de.tillhub.paymentengine

import androidx.lifecycle.DefaultLifecycleObserver
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.SharedFlow
import java.math.BigDecimal

interface CardPaymentManager : DefaultLifecycleObserver {
    fun putTerminalConfig(config: Terminal)
    fun getOrDefault(configName: String): Terminal
    fun observePaymentState(): SharedFlow<TerminalOperationStatus>

    /**
     * This method is called to start of a card payment transaction,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency)
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, configName: String)

    /**
     * This method is called to start of a card payment reversal,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startReversalTransaction(receiptNo: String)
    fun startReversalTransaction(receiptNo: String, configName: String)

    /**
     * This method is called to start of a partial card payment refund,
     * it sets up the manager so the data from the transaction is collected correctly.
     */
    fun startRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency)
    fun startRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, configName: String)

    /**
     * This method is called to start of a terminal reconciliation,
     * it sets up the manager so the data from the operation is collected correctly.
     */
    fun startReconciliation()
    fun startReconciliation(configName: String)
}